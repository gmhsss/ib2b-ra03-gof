import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.editor.*;
import com.change_vision.jude.api.inf.model.*;
import com.change_vision.jude.api.inf.presentation.*;
import com.change_vision.jude.api.inf.project.*;
import java.awt.geom.Point2D;
import java.lang.reflect.*;
import java.util.*;
import java.util.regex.*;

/** Utilitários sobre a API do Astah (com alguns comandos internos para implantação). */
public class Astah {

    final ProjectAccessor pa;
    final BasicModelEditor be;
    final ClassDiagramEditor cde;
    final StateMachineDiagramEditor sde;
    final SequenceDiagramEditor qde;
    final ITransactionManager tm;
    final Map<String, IClass> classes = new LinkedHashMap<>();
    final Map<String, IElement> rels = new HashMap<>();
    final Map<String, IPackage> pkgs = new HashMap<>();
    final List<Object[]> pending = new ArrayList<>();

    /** Fase 2: cria atributos/operações depois que todas as classes existem. */
    void membros(IPackage externos) throws Exception {
        Set<String> prim = new HashSet<>();
        for (INamedElement e : pa.getPrimitiveTypes()) prim.add(e.getName());
        Set<String> ids = new TreeSet<>();
        for (Object[] p : pending) for (String m : (String[]) p[1]) {
            String types = m.contains("(") ? m.substring(m.indexOf('(')) : (m.contains(":") ? m.substring(m.indexOf(':')) : "");
            types = types.replaceAll("«\\w+»", "").replaceAll("=.*", "");
            Matcher mm = Pattern.compile(":\\s*([\\w<>, \\[\\].]+)").matcher(types);
            while (mm.find()) for (String id : mm.group(1).split("[^\\w]+")) if (!id.isEmpty() && Character.isUpperCase(id.charAt(0))) ids.add(id);
        }
        for (String id : ids) if (!classes.containsKey(id) && !prim.contains(id)) {
            IClass x = be.createClass(externos, id);
            classes.put(id, x);
        }
        for (Object[] p : pending) for (String m : (String[]) p[1]) member((IClass) p[0], m);
        pending.clear();
    }

    /** "Optional<Livro>" -> {"Optional", "<Livro>"}; "String[]" -> {"String", "[]"} */
    static String[] split(String t) {
        t = t.trim();
        int i = t.length();
        for (char ch : new char[] { '<', '[', '.' }) { int k = t.indexOf(ch); if (k > 0 && k < i) i = k; }
        return new String[] { t.substring(0, i), t.substring(i) };
    }

    IClass base(String t) {
        String b = split(t)[0];
        IClass c = classes.get(b);
        if (c != null) return c;
        for (INamedElement e : pa.getPrimitiveTypes()) if (e.getName().equals(b)) return (IClass) e;
        return null;
    }

    /** Tipo conhecido do modelo vira referência real; o resto vira expressão (sem criar classes soltas). */
    IClass tipo(String t) {
        return classes.get(t.trim());
    }

    Astah(ProjectAccessor pa) throws Exception {
        this.pa = pa;
        be = pa.getModelEditorFactory().getBasicModelEditor();
        cde = pa.getDiagramEditorFactory().getClassDiagramEditor();
        sde = pa.getDiagramEditorFactory().getStateMachineDiagramEditor();
        qde = pa.getDiagramEditorFactory().getSequenceDiagramEditor();
        tm = pa.getTransactionManager();
    }

    static Point2D pt(double x, double y) {
        return new Point2D.Double(x, y);
    }

    IPackage pkg(IPackage parent, String name) throws Exception {
        String key = parent.getFullName("/") + "/" + name;
        if (!pkgs.containsKey(key)) pkgs.put(key, be.createPackage(parent, name));
        return pkgs.get(key);
    }

    // ---------------- classes ----------------

    /** kind: class | interface | abstract | enum | record */
    IClass cls(IPackage p, String kind, String name, String stereo, String... members) throws Exception {
        IClass c;
        if (kind.equals("interface")) c = be.createInterface(p, name);
        else if (kind.equals("enum")) c = be.createEnumeration(p, name);
        else c = be.createClass(p, name);
        if (kind.equals("abstract")) c.setAbstract(true);
        if (kind.equals("record")) c.addStereotype("record");
        if (stereo != null) c.addStereotype(stereo);
        classes.put(name, c);
        pending.add(new Object[] { c, members });
        return c;
    }

    IClass c(String name) {
        IClass k = classes.get(name);
        if (k == null) throw new IllegalArgumentException("classe desconhecida: " + name);
        return k;
    }

    static final Pattern OP = Pattern.compile("^([+\\-#~])?\\s*(\\{static\\}\\s*|\\{abstract\\}\\s*)*([\\w<>]+)\\((.*)\\)\\s*(?::\\s*(.+?))?\\s*(«\\w+»)?\\s*$");
    static final Pattern AT = Pattern.compile("^([+\\-#~])?\\s*(\\{static\\}\\s*)?(\\w+)\\s*(?::\\s*([^=]+?))?\\s*(?:=\\s*(.+))?$");

    void member(IClass c, String spec) throws Exception {
        if (c instanceof IEnumeration && !spec.contains("(")) {
            be.createEnumerationLiteral((IEnumeration) c, spec.trim());
            return;
        }
        boolean isStatic = spec.contains("{static}");
        boolean isAbstract = spec.contains("{abstract}");
        if (spec.contains("(")) {
            Matcher m = OP.matcher(spec.trim());
            if (!m.matches()) throw new IllegalArgumentException("op: " + spec);
            String ret = m.group(5) == null ? "void" : m.group(5).trim();
            IOperation op = base(ret) != null ? be.createOperation(c, m.group(3), base(ret)) : be.createOperation(c, m.group(3), ret);
            if (!split(ret)[1].isEmpty()) op.setTypeModifier(split(ret)[1]);
            vis(op, m.group(1));
            if (isStatic) op.setStatic(true);
            if (isAbstract) op.setAbstract(true);
            if (m.group(6) != null) {
                String st = m.group(6).replace("«", "").replace("»", "");
                if (st.equals("final")) op.setLeaf(true); // operação folha: não pode ser sobrescrita
                op.addStereotype(st);
            }
            String params = m.group(4).trim();
            if (!params.isEmpty()) for (String prm : splitParams(params)) {
                String[] pv = prm.split(":", 2);
                String pn = pv[0].trim();
                String pt = pv.length > 1 ? pv[1].trim() : "";
                if (pn.endsWith("...")) {
                    pn = pn.substring(0, pn.length() - 3);
                    pt = pt + "...";
                }
                String ptx = pt.isEmpty() ? "Object" : pt;
                IParameter prm2 = base(ptx) != null ? be.createParameter(op, pn, base(ptx)) : be.createParameter(op, pn, ptx);
                if (!split(ptx)[1].isEmpty()) prm2.setTypeModifier(split(ptx)[1]);
            }
        } else {
            Matcher m = AT.matcher(spec.replace("{static}", "").trim());
            if (!m.matches()) throw new IllegalArgumentException("attr: " + spec);
            String type = m.group(4) == null ? "" : m.group(4).trim();
            String tx = type.isEmpty() ? "String" : type;
            IAttribute a;
            a = base(tx) != null ? be.createAttribute(c, m.group(3), base(tx)) : be.createAttribute(c, m.group(3), tx);
            if (!split(tx)[1].isEmpty()) a.setTypeModifier(split(tx)[1]);
            vis(a, m.group(1));
            if (isStatic) a.setStatic(true);
            if (m.group(5) != null) a.setInitialValue(m.group(5).trim());
        }
    }

    static List<String> splitParams(String s) {
        List<String> out = new ArrayList<>();
        int depth = 0, start = 0;
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (ch == '<') depth++;
            if (ch == '>') depth--;
            if (ch == ',' && depth == 0) {
                out.add(s.substring(start, i));
                start = i + 1;
            }
        }
        out.add(s.substring(start));
        return out;
    }

    static void vis(INamedElement e, String v) throws Exception {
        if (v == null) return;
        e.setVisibility(switch (v) {
            case "+" -> "public";
            case "-" -> "private";
            case "#" -> "protected";
            default -> "package";
        });
    }

    // ---------------- relações (criadas uma vez, reaproveitadas) ----------------

    IElement gen(String sub, String sup) throws Exception {
        return rel("G|" + sub + "|" + sup, () -> be.createGeneralization(c(sub), c(sup), ""));
    }

    IElement real(String impl, String iface) throws Exception {
        return rel("R|" + impl + "|" + iface, () -> be.createRealization(c(impl), c(iface), ""));
    }

    IElement dep(String client, String supplier, String label) throws Exception {
        return rel("D|" + client + "|" + supplier + "|" + label, () -> be.createDependency(c(supplier), c(client), label));
    }

    /** kind: assoc | agg | comp ; a = lado do todo/origem, b = destino navegável */
    IElement assoc(String kind, String a, String b, String label, String multB) throws Exception {
        return rel("A|" + kind + "|" + a + "|" + b + "|" + label, () -> {
            IAssociation as = be.createAssociation(c(a), c(b), label, "", "");
            for (IAttribute end : as.getMemberEnds()) {
                if (end.getType() == c(b) && !a.equals(b) || a.equals(b) && end == as.getMemberEnds()[1]) {
                    end.setNavigability("Navigable");
                    if (multB != null && !multB.isEmpty()) end.setMultiplicityString(multB);
                } else {
                    end.setNavigability("Unspecified");
                    if (kind.equals("comp")) end.setComposite();
                    if (kind.equals("agg")) end.setAggregation();
                }
            }
            return as;
        });
    }

    interface Mk {
        IElement make() throws Exception;
    }

    IElement rel(String key, Mk mk) throws Exception {
        if (!rels.containsKey(key)) rels.put(key, mk.make());
        return rels.get(key);
    }

    // ---------------- diagramas de classes ----------------

    Map<String, INodePresentation> nodes;

    IDiagram classDiagram(IPackage owner, String name) throws Exception {
        IDiagram d = cde.createClassDiagram(owner, name);
        cde.setDiagram(d);
        nodes = new HashMap<>();
        return d;
    }

    INodePresentation put(String cls, double x, double y) throws Exception {
        INodePresentation np = cde.createNodePresentation(c(cls), pt(x, y));
        retangulo(np);
        nodes.put(cls, np);
        return np;
    }

    /** Classe de apoio: só nome e estereótipo (sem compartimentos). */
    INodePresentation putC(String cls, double x, double y) throws Exception {
        INodePresentation np = put(cls, x, y);
        np.setProperty("attribute_compartment_visibility", "false");
        np.setProperty("operation_compartment_visibility", "false");
        return np;
    }

    static void retangulo(IPresentation p) {
        try {
            if ("icon".equals(p.getProperty("notation_type"))) p.setProperty("notation_type", "normal");
        } catch (Exception e) {
            System.out.println("aviso notation: " + e.getMessage());
        }
    }

    INodePresentation n(String cls) {
        INodePresentation p = nodes.get(cls);
        if (p == null) throw new IllegalArgumentException("sem presença no diagrama: " + cls);
        return p;
    }

    void link(IElement rel, String from, String to) throws Exception {
        cde.createLinkPresentation(rel, n(from), n(to));
    }

    void G(String sub, String sup) throws Exception {
        cde.createLinkPresentation(gen(sub, sup), n(sub), n(sup));
    }

    void R(String impl, String iface) throws Exception {
        cde.createLinkPresentation(real(impl, iface), n(impl), n(iface));
    }

    void D(String client, String supplier, String label) throws Exception {
        cde.createLinkPresentation(dep(client, supplier, label), n(supplier), n(client));
    }

    ILinkPresentation A(String kind, String a, String b, String label, String multB) throws Exception {
        return cde.createLinkPresentation(assoc(kind, a, b, label, multB), n(a), n(b));
    }

    void note(BasicDiagramEditor ed, String text, double x, double y, IPresentation anchor) throws Exception {
        INodePresentation np = ed.createNote(text, pt(x, y));
        if (anchor != null) ed.createNoteAnchor(np, anchor);
    }

    // ---------------- comandos internos (implantação) ----------------

    static Object call(Object o, String name, Object... args) throws Exception {
        for (Class<?> k = o.getClass(); k != null; k = k.getSuperclass()) for (Method m : k.getDeclaredMethods()) {
            if (!m.getName().equals(name) || m.getParameterCount() != args.length) continue;
            Class<?>[] pt = m.getParameterTypes();
            boolean ok = true;
            for (int i = 0; i < pt.length; i++) if (args[i] != null && !pt[i].isPrimitive() && !pt[i].isInstance(args[i])) ok = false;
            if (!ok) continue;
            m.setAccessible(true);
            return m.invoke(o, args);
        }
        throw new NoSuchMethodException(name + " em " + o.getClass());
    }

    IDiagram deploymentDiagram(IPackage owner, String name) throws Exception {
        Object es = call(pa, "k");
        Object ume = call(owner, "j");
        Object cmd = Class.forName("JP.co.esm.caddies.jomt.jcontrol.CreateDeploymentDiagramFromPrjCommand").getDeclaredConstructor().newInstance();
        Method cr = null;
        for (Method m : cmd.getClass().getDeclaredMethods()) if (m.getName().equals("a") && m.getParameterCount() == 2) cr = m;
        cr.setAccessible(true);
        Set<IDiagram> before = new HashSet<>(Arrays.asList(owner.getDiagrams()));
        cr.invoke(cmd, es, ume);
        IDiagram d = null;
        for (IDiagram x : owner.getDiagrams()) if (!before.contains(x)) d = x;
        d.setName(name);
        cde.setDiagram(d);
        nodes = new HashMap<>();
        return d;
    }

    /** Instância de nó (NodeInstance), opcionalmente dentro de outra. */
    INodePresentation nodeInstance(String key, String name, INode type, INodePresentation container, double x, double y) throws Exception {
        IDiagram d = cde.getDiagram();
        Object ud = call(d, "g");
        Object pres = Class.forName("JP.co.esm.caddies.jomt.jmodel.NodeInstancePresentation").getDeclaredConstructor().newInstance();
        Class<?> pnt = Class.forName("JP.co.esm.caddies.golf.geom2D.Pnt2d");
        call(pres, "setLocation", pnt.getConstructor(double.class, double.class).newInstance(x, y));
        Object cmd = Class.forName("JP.co.esm.caddies.jomt.jcontrol.CreateNodeInstanceCommand").getDeclaredConstructor().newInstance();
        Class<?> sup = cmd.getClass().getSuperclass();
        Method setPres = sup.getDeclaredMethod("a", Class.forName("JP.co.esm.caddies.uml.Foundation.Core.IUPresentation"));
        setPres.setAccessible(true);
        setPres.invoke(cmd, pres);
        Method setDiag = sup.getDeclaredMethod("a", Class.forName("JP.co.esm.caddies.uml.Foundation.Core.UDiagram"));
        setDiag.setAccessible(true);
        setDiag.invoke(cmd, ud);
        if (container != null) {
            Method setCont = sup.getDeclaredMethod("a", Class.forName("JP.co.esm.caddies.jomt.jmodel.IJomtPresentation"));
            setCont.setAccessible(true);
            setCont.invoke(cmd, call(container, "d"));
        }
        call(cmd, "setUseTransaction", false); // já estamos dentro da transação da API
        call(cmd, "execute");
        INodePresentation np = null;
        for (IPresentation p : d.getPresentations()) if (p instanceof INodePresentation && call(p, "d") == pres) np = (INodePresentation) p;
        IInstanceSpecification is = (IInstanceSpecification) np.getModel();
        is.setName(name);
        is.setClassifier(type);
        nodes.put(key, np);
        return np;
    }

    INodePresentation putEl(String key, IElement el, INodePresentation parent, double x, double y) throws Exception {
        INodePresentation np = parent == null ? cde.createNodePresentation(el, pt(x, y)) : cde.createNodePresentation(el, parent, pt(x, y));
        nodes.put(key, np);
        return np;
    }

    // ---------------- sequência ----------------

    Map<String, INodePresentation> lines;
    double y;

    IDiagram seqDiagram(IPackage owner, String name) throws Exception {
        IDiagram d = qde.createSequenceDiagram(owner, name);
        qde.setDiagram(d);
        lines = new LinkedHashMap<>();
        y = 110;
        return d;
    }

    /** "obj:Classe", ":Classe" ou "Nome" (sem classe base) */
    void lifeline(String key, String label, double x) throws Exception {
        String nm = label, base = null;
        if (label.contains(":")) {
            nm = label.substring(0, label.indexOf(':')).trim();
            base = label.substring(label.indexOf(':') + 1).trim();
        }
        INodePresentation lp = qde.createLifeline(nm, x);
        retangulo(lp);
        if (base != null && classes.containsKey(base)) {
            ((ILifeline) lp.getModel()).setBase(c(base));
            retangulo(lp);
        }
        else if (base != null) ((ILifeline) lp.getModel()).setName(label);
        lines.put(key, lp);
    }

    static final Pattern MSG = Pattern.compile("^(.*?)\\s*(?:\\[(.*)\\])?\\s*$");
    static final Pattern CALL = Pattern.compile("^([\\w.]+)\\((.*)\\)$");

    ILinkPresentation msg(String from, String to, String text) throws Exception {
        Matcher mg = MSG.matcher(text.trim());
        mg.matches();
        String body = mg.group(1).trim(), guard = mg.group(2);
        Matcher mc = CALL.matcher(body);
        String name = body, arg = null;
        if (mc.matches()) {
            name = mc.group(1);
            arg = mc.group(2);
        }
        ILinkPresentation lp = qde.createMessage(name, lines.get(from), lines.get(to), y);
        IMessage im = (IMessage) lp.getModel();
        if (arg != null && !arg.isEmpty()) im.setArgument(arg);
        if (guard != null) im.setGuard(guard);
        y += from.equals(to) ? 55 : 40;
        return lp;
    }

    ILinkPresentation ret(ILinkPresentation call, String text) throws Exception {
        ILinkPresentation r = qde.createReturnMessage(text, call);
        y += 30;
        return r;
    }
}
