import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.model.*;
import com.change_vision.jude.api.inf.presentation.*;
import com.change_vision.jude.api.inf.project.*;
import java.util.*;

/**
 * Gera o pacote "RA03 — Padrões GoF" dentro de uma CÓPIA do projeto Astah do RA02.
 * Uso: java GerarRA03 <projeto-base.asta> <saida.asta>
 */
public class GerarRA03 {

    static Astah g;

    public static void main(String[] a) throws Exception {
        int code = 0;
        try {
            run(a);
        } catch (Throwable t) {
            t.printStackTrace(System.out);
            code = 1;
        }
        System.exit(code);
    }

    static void run(String[] a) throws Exception {
        ProjectAccessor pa = AstahAPI.getAstahAPI().getProjectAccessor();
        pa.open(a[0], true, false, true);
        g = new Astah(pa);
        IModel root = pa.getProject();
        g.tm.beginTransaction();
        IPackage ra03 = g.pkg(root, "RA03 — Padrões GoF");
        IPackage modelo = g.pkg(ra03, "Modelo (código Java)");
        modelo(modelo);
        g.tm.endTransaction();

        IPackage est = sub(ra03, "1 Estados");
        IPackage cls = sub(ra03, "2 Classes por padrão");
        IPackage seq = sub(ra03, "3 Sequências");
        IPackage imp = sub(ra03, "4 Implantação");

        step(() -> maquinaDeEstados(est));
        step(() -> r01(cls));
        step(() -> r02(cls));
        step(() -> r03(cls));
        step(() -> r04(cls));
        step(() -> r05(cls));
        step(() -> r06(cls));
        step(() -> r07(cls));
        step(() -> r08(cls));
        step(() -> r09(cls));
        step(() -> r10(cls));
        step(() -> seqR01(seq));
        step(() -> seqR02(seq));
        step(() -> seqR03(seq));
        step(() -> seqR04(seq));
        step(() -> seqR05(seq));
        step(() -> seqR06(seq));
        step(() -> seqR07(seq));
        step(() -> seqR08(seq));
        step(() -> seqR09(seq));
        step(() -> seqR10(seq));
        step(() -> implantacaoTipos(imp));
        step(() -> implantacaoInstancias(imp));

        pa.saveAs(a[1]);
        pa.close();
        System.out.println("OK: " + a[1]);
    }

    interface Step {
        void run() throws Exception;
    }

    static void step(Step s) throws Exception {
        g.tm.beginTransaction();
        try {
            s.run();
            g.tm.endTransaction();
        } catch (Exception e) {
            g.tm.abortTransaction();
            throw e;
        }
    }

    static IPackage sub(IPackage p, String n) throws Exception {
        g.tm.beginTransaction();
        IPackage r = g.pkg(p, n);
        g.tm.endTransaction();
        return r;
    }

    // =====================================================================
    // MODELO: cada classe existe uma única vez e é reutilizada nos diagramas
    // =====================================================================

    static void modelo(IPackage m) throws Exception {
        IPackage dom = g.pkg(m, "dominio"), est = g.pkg(m, "estado"), per = g.pkg(m, "persistencia");
        IPackage ser = g.pkg(m, "servico"), pad = g.pkg(m, "padroes"), app = g.pkg(m, "app");

        // ---- dominio
        g.cls(dom, "class", "Livro", "Context",
            "-id: String", "-titulo: String", "-estado: EstadoDoLivro",
            "+estado(): EstadoLivro", "+reservar()", "+emprestar()", "+renovar(e: Emprestimo)", "+devolver()",
            "+inspecionar(apto: boolean)", "+encaminharParaReparo()", "+concluirReparo()", "+laudoIrrecuperavel()",
            "+verificarExclusao()", "+alterarEstado(novo: EstadoLivro)");
        g.cls(dom, "class", "Emprestimo", null,
            "+{static} LIMITE_RENOVACOES: int = 2", "+{static} DIAS_POR_PERIODO: int = 30",
            "-vencimento: LocalDate", "-numRenovacoes: int", "-encerrado: boolean",
            "+numRenovacoes(): int", "+prorrogar()", "+encerrar()");
        g.cls(dom, "enum", "EstadoLivro", null, "LIVRE", "RESERVADO", "EMPRESTADO", "RENOVADO", "DEVOLVIDO", "REPARANDO", "INCINERADO");
        g.cls(dom, "class", "BoletoMulta", null,
            "-codigo: String", "-valor: BigDecimal", "-politica: String", "-codigoExterno: String", "-quitado: boolean",
            "+quitar(): boolean");
        g.cls(dom, "record", "Devolucao", null, "+idLivro: String", "+idEstudante: String", "+vencimento: LocalDate",
            "+dataDevolucao: LocalDate", "+diasAtraso(): long");
        g.cls(dom, "record", "Reserva", null, "+idLivro: String", "+idEstudante: String", "+criadaEm: LocalDate");
        g.cls(dom, "record", "Comprovante", null, "+operacao: String", "+livro: String", "+estudante: String",
            "+data: LocalDate", "+detalhe: String");

        // ---- estado (State)
        g.cls(est, "interface", "EstadoDoLivro", "State",
            "+tipo(): EstadoLivro", "+reservar(l: Livro)", "+emprestar(l: Livro)", "+renovar(l: Livro, e: Emprestimo)",
            "+devolver(l: Livro)", "+inspecionar(l: Livro, apto: boolean)", "+encaminharParaReparo(l: Livro)",
            "+concluirReparo(l: Livro)", "+laudoIrrecuperavel(l: Livro)", "+verificarExclusao(l: Livro)");
        g.cls(est, "class", "Livre", "ConcreteState", "+reservar(l: Livro)", "+encaminharParaReparo(l: Livro)", "+verificarExclusao(l: Livro)");
        g.cls(est, "class", "Reservado", "ConcreteState", "+emprestar(l: Livro)");
        g.cls(est, "class", "Emprestado", "ConcreteState", "+renovar(l: Livro, e: Emprestimo)", "+devolver(l: Livro)");
        g.cls(est, "class", "Renovado", "ConcreteState", "+renovar(l: Livro, e: Emprestimo)", "+devolver(l: Livro)");
        g.cls(est, "class", "Devolvido", "ConcreteState", "+inspecionar(l: Livro, apto: boolean)");
        g.cls(est, "class", "Reparando", "ConcreteState", "+concluirReparo(l: Livro)", "+laudoIrrecuperavel(l: Livro)");
        g.cls(est, "class", "Incinerado", "ConcreteState");
        g.cls(est, "class", "Estados", null, "+{static} doTipo(t: EstadoLivro): EstadoDoLivro");

        // ---- persistencia (Abstract Factory)
        g.cls(per, "interface", "FabricaPersistencia", "AbstractFactory",
            "+livros(): LivroRepositorio", "+movimentacao(): MovimentacaoRepositorio",
            "+relacionamento(): RelacionamentoRepositorio", "+unidadeDeTrabalho(): UnidadeDeTrabalho", "+familia(): String");
        g.cls(per, "class", "FabricaMemoria", "ConcreteFactory");
        g.cls(per, "class", "FabricaMySQL", "ConcreteFactory", "-fonte: FonteDeConexoes", "-atual: ThreadLocal<Connection>");
        g.cls(per, "interface", "LivroRepositorio", "AbstractProduct", "+buscar(id: String): Optional<Livro>", "+salvar(l: Livro)");
        g.cls(per, "interface", "MovimentacaoRepositorio", "AbstractProduct", "+salvarReserva(r: Reserva)",
            "+salvarEmprestimo(e: Emprestimo)", "+salvarBoleto(b: BoletoMulta)");
        g.cls(per, "interface", "RelacionamentoRepositorio", "AbstractProduct", "+estudante(id: String): Optional<Estudante>",
            "+salvarEstudante(e: Estudante)");
        g.cls(per, "interface", "UnidadeDeTrabalho", "AbstractProduct", "+executar(op: Supplier<T>): T");

        // ---- servico
        g.cls(ser, "class", "CirculacaoFacade", "Facade",
            "+reservar(livro: String, estudante: String, dia: LocalDate): Comprovante",
            "+efetivarEmprestimo(livro: String, estudante: String, dia: LocalDate): Comprovante",
            "+renovar(livro: String, estudante: String, dia: LocalDate): Comprovante",
            "+devolver(livro: String, estudante: String, dia: LocalDate): Comprovante",
            "+inspecionar(livro: String, apto: boolean, dia: LocalDate): Comprovante",
            "+reservaAtiva(livro: String): Optional<Reserva>",
            "+exigirDevolucao(livro: String, estudante: String): Devolucao");
        g.c("CirculacaoFacade").addStereotype("Receiver");
        g.cls(ser, "class", "ServicoMultas", "Context",
            "-politica: PoliticaMulta", "-banco: GatewayCobranca",
            "+configurarPolitica(nova: PoliticaMulta)", "+emitir(d: Devolucao): Optional<BoletoMulta>",
            "+registrarNotificacao(msg: String): boolean");
        g.c("ServicoMultas").addStereotype("Client");
        g.cls(ser, "class", "Acervo", null, "+exigir(id: String): Livro", "+atualizar(l: Livro)");
        g.cls(ser, "class", "Movimentacao", null, "+exigirReserva(livro: String, estudante: String): Reserva",
            "+removerReserva(livro: String)", "+emprestar(e: Emprestimo)", "+devolver(d: Devolucao)");
        g.cls(ser, "class", "Relacionamento", null, "+exigirEstudante(id: String): Estudante");
        for (String i : List.of("iLivro", "iPessoa", "iEstudante", "iAtendente")) g.cls(ser, "interface", i, null);
        g.cls(ser, "class", "TratadorDeAcervo", null);
        g.cls(ser, "class", "TratadorDePessoa", null);
        g.cls(ser, "class", "TratadorDeEstudante", null);
        g.cls(ser, "class", "TratadorDeAtendente", null);
        g.cls(ser, "class", "TratadorDeEmprestimo", "Client", "+processar(c: SolicitacaoCirculacao): Comprovante");
        g.cls(ser, "class", "TratadorDeDevolucao", "Client");

        // ---- padroes: Strategy
        g.cls(pad, "interface", "PoliticaMulta", "Strategy", "+nome(): String", "+calcular(diasDeAtraso: long): BigDecimal");
        g.cls(pad, "class", "MultaOriginal", "ConcreteStrategy");
        g.cls(pad, "class", "MultaComTeto", "ConcreteStrategy");
        g.cls(pad, "class", "MultaComTolerancia", "ConcreteStrategy");
        // Adapter
        g.cls(pad, "interface", "GatewayCobranca", "Target",
            "+emitir(codigo: String, reais: BigDecimal, cpf: String): String",
            "+traduzirNotificacao(msg: String): NotificacaoPagamento");
        g.cls(pad, "class", "AdaptadorBancoA", "Adapter", "+emitir(codigo: String, reais: BigDecimal, cpf: String): String",
            "+traduzirNotificacao(msg: String): NotificacaoPagamento");
        g.cls(pad, "class", "AdaptadorBancoB", "Adapter", "+emitir(codigo: String, reais: BigDecimal, cpf: String): String",
            "+traduzirNotificacao(msg: String): NotificacaoPagamento");
        g.cls(pad, "class", "BancoASimulado", "Adaptee", "+gerarCobranca(nossoNumero: String, centavos: int, cpfDigitos: String): String");
        g.cls(pad, "class", "BancoBSimulado", "Adaptee", "+criarBoleto(cpfFormatado: String, valorReais: BigDecimal, referencia: String): String");
        g.cls(pad, "record", "NotificacaoPagamento", null, "+codigoExterno: String", "+situacao: SituacaoCobranca");
        g.cls(pad, "enum", "SituacaoCobranca", null, "PAGA", "CANCELADA");
        // Observer
        g.cls(pad, "class", "PublicadorDevolucao", "Subject", "+registrar(o: OuvinteDevolucao)", "+remover(o: OuvinteDevolucao)",
            "+publicar(ev: EventoDevolucao)", "+reenviarFalhas(): int", "+falhas(): List<FalhaNotificacao>");
        g.cls(pad, "interface", "OuvinteDevolucao", "Observer", "+aoDevolver(ev: EventoDevolucao)");
        g.cls(pad, "record", "EventoDevolucao", null, "+devolucao: Devolucao", "+idLivro(): String", "+idEstudante(): String",
            "+vencimento(): LocalDate", "+dataDevolucao(): LocalDate", "+diasAtraso(): long");
        g.cls(pad, "record", "FalhaNotificacao", null, "+ouvinte: OuvinteDevolucao", "+evento: EventoDevolucao", "+erro: RuntimeException");
        for (String o : List.of("OuvinteInspecao", "OuvinteMultas", "OuvinteIndicadores", "OuvinteEmail"))
            g.cls(pad, "class", o, "ConcreteObserver", "+aoDevolver(ev: EventoDevolucao)");
        // Command
        g.cls(pad, "interface", "SolicitacaoCirculacao", "Command", "+identificador(): UUID", "+autor(): String",
            "+criadaEm(): Instant", "+executar(f: CirculacaoFacade): Comprovante");
        g.cls(pad, "record", "SolicitacaoReserva", "ConcreteCommand", "+livro: String", "+dia: LocalDate",
            "+executar(f: CirculacaoFacade): Comprovante");
        g.cls(pad, "record", "SolicitacaoRenovacao", "ConcreteCommand", "+livro: String", "+dia: LocalDate",
            "+executar(f: CirculacaoFacade): Comprovante");
        g.cls(pad, "class", "ExecutorSolicitacoes", "Invoker", "-feitos: Map<UUID, Comprovante>",
            "+executar(c: SolicitacaoCirculacao): Comprovante");
        // Template Method
        g.cls(pad, "abstract", "CadastroAbstrato", "AbstractClass",
            "+incluir(entrada...: String): T «final»", "#{abstract} buscarExistente(e: String[]): Optional<T>",
            "#{abstract} validar(e: String[])", "#verificarPreRequisitos(e: String[]) «gancho»",
            "#{abstract} criar(e: String[]): T", "#{abstract} armazenar(obj: T)", "#apresentar(obj: T): T «gancho»");
        try {
            g.be.createTemplateParameter(g.c("CadastroAbstrato"), "T", (IClass) null, null);
        } catch (Exception e) {
            System.out.println("aviso: parâmetro de template não criado: " + e.getMessage());
        }
        g.cls(pad, "class", "CadastroLivro", "ConcreteClass");
        g.cls(pad, "class", "CadastroPessoa", "ConcreteClass");
        g.cls(pad, "class", "CadastroEstudante", "ConcreteClass", "#verificarPreRequisitos(e: String[])");
        g.cls(pad, "class", "CadastroAtendente", "ConcreteClass", "#verificarPreRequisitos(e: String[])");
        // Decorator
        g.cls(pad, "interface", "ConsultaAcervo", "Component", "+consultar(id: String): Optional<LivroInfo>", "+invalidar(id: String)");
        g.cls(pad, "class", "ConsultaOriginal", "ConcreteComponent", "-repo: LivroRepositorio");
        g.cls(pad, "abstract", "DecoradorConsulta", "Decorator", "#interno: ConsultaAcervo");
        g.cls(pad, "class", "ConsultaComCache", "ConcreteDecorator", "-cache: Map<String, LivroInfo>", "+invalidar(id: String)");
        g.cls(pad, "class", "ConsultaComMedicao", "ConcreteDecorator", "-chamadas: AtomicInteger", "-nanosAcumulados: AtomicLong");
        g.cls(pad, "record", "LivroInfo", null, "+id: String", "+titulo: String");
        // Composite
        g.cls(pad, "interface", "ElementoAcervo", "Component", "+contar(): int", "+listarIds(): List<String>");
        g.cls(pad, "class", "GrupoAcervo", "Composite", "-nome: String", "-pai: GrupoAcervo",
            "-donoDeLivro: Map<String, GrupoAcervo>", "+novaSecao(nome: String): GrupoAcervo",
            "+anexarGrupo(outro: GrupoAcervo)", "+moverPara(destino: GrupoAcervo)", "+incluirLivro(idLivro: String)",
            "+removerLivro(idLivro: String)", "+transferirLivro(idLivro: String, destino: GrupoAcervo)");
        g.cls(pad, "class", "ItemAcervo", "Leaf", "-livroId: String");

        // ---- app
        g.cls(app, "class", "SistemaIB2B", null,
            "+{static} inicializar(perfil: String, banco: String, politica: String): SistemaIB2B",
            "+{static} montarConsulta(repo: LivroRepositorio, config: String): ConsultaAcervo");
        g.cls(app, "class", "Main", "Client", "+{static} main(args: String[])");
        g.membros(g.pkg(m, "tipos externos (Java)"));
    }

    // =====================================================================
    // 1 — MÁQUINA DE ESTADOS
    // =====================================================================

    static void maquinaDeEstados(IPackage p) throws Exception {
        var sde = g.sde;
        sde.createStatemachineDiagram(p, "00 Máquina de estados de Livro");
        Map<String, INodePresentation> s = new HashMap<>();
        INodePresentation ini = sde.createInitialPseudostate(null, Astah.pt(90, 95));
        s.put("LIVRE", sde.createState("LIVRE", null, Astah.pt(220, 80)));
        s.put("RESERVADO", sde.createState("RESERVADO", null, Astah.pt(520, 80)));
        s.put("EMPRESTADO", sde.createState("EMPRESTADO", null, Astah.pt(860, 80)));
        s.put("RENOVADO", sde.createState("RENOVADO", null, Astah.pt(1140, 380)));
        s.put("DEVOLVIDO", sde.createState("DEVOLVIDO", null, Astah.pt(560, 380)));
        s.put("REPARANDO", sde.createState("REPARANDO", null, Astah.pt(200, 380)));
        s.put("INCINERADO", sde.createState("INCINERADO", null, Astah.pt(200, 600)));
        INodePresentation fim = sde.createFinalState(null, Astah.pt(470, 615));
        tr(sde.createTransition(ini, s.get("LIVRE")), null, null, "cadastrar");
        tr(sde.createTransition(s.get("LIVRE"), s.get("RESERVADO")), "reservar", null, null);
        tr(sde.createTransition(s.get("RESERVADO"), s.get("EMPRESTADO")), "emprestar", "reserva do estudante", null);
        tr(sde.createTransition(s.get("EMPRESTADO"), s.get("RENOVADO")), "renovar", "numRenovacoes < 2", "prorrogar 30 dias");
        ILinkPresentation self = sde.createTransition(s.get("RENOVADO"), s.get("RENOVADO"));
        tr(self, "renovar", "numRenovacoes < 2", "prorrogar 30 dias");
        tr(sde.createTransition(s.get("EMPRESTADO"), s.get("DEVOLVIDO")), "devolver", null, "publicar EventoDevolucao");
        tr(sde.createTransition(s.get("RENOVADO"), s.get("DEVOLVIDO")), "devolver", null, "publicar EventoDevolucao");
        tr(sde.createTransition(s.get("DEVOLVIDO"), s.get("LIVRE")), "inspecionar", "apto", null);
        tr(sde.createTransition(s.get("DEVOLVIDO"), s.get("REPARANDO")), "inspecionar", "danificado", null);
        ILinkPresentation enc = sde.createTransition(s.get("LIVRE"), s.get("REPARANDO"));
        tr(enc, "encaminharParaReparo", null, null);
        double lx = s.get("LIVRE").getLocation().getX();
        enc.setPoints(new java.awt.geom.Point2D[] {
            Astah.pt(lx, s.get("LIVRE").getLocation().getY() + 20), Astah.pt(110, 250), Astah.pt(s.get("REPARANDO").getLocation().getX(), s.get("REPARANDO").getLocation().getY() + 20) });
        tr(sde.createTransition(s.get("REPARANDO"), s.get("LIVRE")), "concluirReparo", null, null);
        tr(sde.createTransition(s.get("REPARANDO"), s.get("INCINERADO")), "laudoIrrecuperavel", null, null);
        ILinkPresentation fimT = sde.createTransition(s.get("INCINERADO"), fim);
        try {
            fimT.setLabel("");
        } catch (Exception e) {
            System.out.println("aviso rótulo final: " + e.getMessage());
        }
        g.note(sde, "Não aceita reservar antes da inspeção.", 560, 500, s.get("DEVOLVIDO"));
        g.note(sde, "Guarda e ação ficam no método renovar()\nda classe do estado de origem\n(Emprestado / Renovado); a guarda é\navaliada ANTES de prorrogar o vencimento.", 1010, 560, s.get("RENOVADO"));
    }

    static void tr(ILinkPresentation lp, String ev, String guard, String act) throws Exception {
        ITransition t = (ITransition) lp.getModel();
        if (ev != null) t.setEvent(ev);
        if (guard != null) t.setGuard(guard);
        if (act != null) t.setAction(act);
    }

    // =====================================================================
    // 2 — DIAGRAMAS DE CLASSES
    // =====================================================================

    static void r01(IPackage p) throws Exception {
        g.classDiagram(p, "R01 State — ciclo de vida do Livro");
        g.put("Livro", 1000, 20);
        g.put("Estados", 1500, 120);
        g.put("EstadoLivro", 1560, 330);
        g.put("EstadoDoLivro", 980, 420);
        String[] st = { "Livre", "Reservado", "Emprestado", "Renovado", "Devolvido", "Reparando", "Incinerado" };
        double[] xs = { 20, 390, 665, 1055, 1445, 1855, 2200 };
        for (int i = 0; i < st.length; i++) g.put(st[i], xs[i], 760);
        g.put("Emprestimo", 860, 950);
        g.A("comp", "Livro", "EstadoDoLivro", "estado atual", "1");
        for (String s : st) g.R(s, "EstadoDoLivro");
        g.D("Livro", "Estados", "reconstitui a partir do enum gravado");
        g.D("Estados", "EstadoLivro", "");
        g.D("Emprestado", "Emprestimo", "guarda [numRenovacoes < 2] / prorrogar()");
        g.D("Renovado", "Emprestimo", "");
        g.note(g.cde, "Métodos default da interface lançam\nRegraNegocioException sem alterar o livro:\ncada estado sobrescreve só o que aceita.", 480, 450, g.n("EstadoDoLivro"));
    }

    static void r02(IPackage p) throws Exception {
        g.classDiagram(p, "R02 Strategy — políticas de multa");
        g.putC("SistemaIB2B", 20, 40);
        g.put("ServicoMultas", 420, 20);
        g.put("BoletoMulta", 980, 20);
        g.put("PoliticaMulta", 460, 300);
        g.put("MultaOriginal", 240, 480);
        g.put("MultaComTeto", 540, 480);
        g.put("MultaComTolerancia", 820, 480);
        g.A("agg", "ServicoMultas", "PoliticaMulta", "vigente", "1");
        for (String s : List.of("MultaOriginal", "MultaComTeto", "MultaComTolerancia")) g.R(s, "PoliticaMulta");
        g.D("ServicoMultas", "BoletoMulta", "cria com valor e nome da política");
        g.D("SistemaIB2B", "PoliticaMulta", "escolhe por IB2B_POLITICA");
        g.note(g.cde, "R$ 1,00 por dia", 220, 590, g.n("MultaOriginal"));
        g.note(g.cde, "R$ 1,00/dia, máx. R$ 20,00", 500, 590, g.n("MultaComTeto"));
        g.note(g.cde, "R$ 1,00/dia após 2 dias", 820, 590, g.n("MultaComTolerancia"));
    }

    static void r03(IPackage p) throws Exception {
        g.classDiagram(p, "R03 Adapter — bancos incompatíveis");
        g.put("ServicoMultas", 20, 20);
        g.put("GatewayCobranca", 660, 40);
        g.put("NotificacaoPagamento", 1300, 40);
        g.put("SituacaoCobranca", 1360, 220);
        g.put("AdaptadorBancoA", 20, 290);
        g.put("AdaptadorBancoB", 760, 290);
        g.put("BancoASimulado", 20, 520);
        g.put("BancoBSimulado", 760, 520);
        g.A("assoc", "ServicoMultas", "GatewayCobranca", "sem if por banco", "1");
        g.R("AdaptadorBancoA", "GatewayCobranca");
        g.R("AdaptadorBancoB", "GatewayCobranca");
        g.A("assoc", "AdaptadorBancoA", "BancoASimulado", "", "1");
        g.A("assoc", "AdaptadorBancoB", "BancoBSimulado", "", "1");
        g.D("GatewayCobranca", "NotificacaoPagamento", "");
        g.A("assoc", "NotificacaoPagamento", "SituacaoCobranca", "", "1");
        g.note(g.cde, "valor: 7.00 -> 700 centavos\nCPF: só dígitos; id: alfanumérico\n\"LIQ|A-...\" -> PAGA", 340, 400, g.n("AdaptadorBancoA"));
        g.note(g.cde, "valor: 7.00 -> 7.00 reais\nCPF: 000.000.000-00\n\"status=pago;boleto=B/...\" -> PAGA", 1080, 400, g.n("AdaptadorBancoB"));
    }

    static void r04(IPackage p) throws Exception {
        g.classDiagram(p, "R04 Facade — entrada única da circulação");
        g.putC("TratadorDeEmprestimo", 140, 20);
        g.putC("TratadorDeDevolucao", 560, 20);
        g.put("CirculacaoFacade", 200, 150);
        g.putC("PublicadorDevolucao", 1000, 140);
        g.putC("Comprovante", 1000, 240);
        g.put("Relacionamento", 20, 480);
        g.put("Movimentacao", 390, 480);
        g.put("Acervo", 920, 480);
        g.put("UnidadeDeTrabalho", 1000, 330);
        g.A("assoc", "TratadorDeEmprestimo", "CirculacaoFacade", "", "1");
        g.A("assoc", "TratadorDeDevolucao", "CirculacaoFacade", "", "1");
        g.A("assoc", "CirculacaoFacade", "Relacionamento", "1", "1");
        g.A("assoc", "CirculacaoFacade", "Movimentacao", "2, 4, 5", "1");
        g.A("assoc", "CirculacaoFacade", "Acervo", "3, 6", "1");
        g.A("assoc", "CirculacaoFacade", "UnidadeDeTrabalho", "commit / rollback", "1");
        g.A("assoc", "CirculacaoFacade", "PublicadorDevolucao", "após o commit", "1");
        g.D("CirculacaoFacade", "Comprovante", "devolve");
        g.note(g.cde, "CirculacaoFacade.efetivarEmprestimo:\n1 estudante existe   2 a reserva é dele\n3 livro.emprestar()   4 remove a reserva\n5 cria o empréstimo   6 grava o livro\nFalha em qualquer passo => rollback total.", 20, 660, null);
    }

    static void r05(IPackage p) throws Exception {
        g.classDiagram(p, "R05 Observer — reações à devolução");
        g.putC("CirculacaoFacade", 20, 40);
        g.put("PublicadorDevolucao", 480, 20);
        g.put("FalhaNotificacao", 1000, 20);
        g.put("EventoDevolucao", 1000, 190);
        g.put("OuvinteDevolucao", 520, 300);
        g.put("OuvinteInspecao", 20, 470);
        g.put("OuvinteMultas", 380, 470);
        g.put("OuvinteIndicadores", 740, 470);
        g.put("OuvinteEmail", 1100, 470);
        g.putC("ServicoMultas", 420, 640);
        g.A("assoc", "CirculacaoFacade", "PublicadorDevolucao", "publicar() após commit", "1");
        g.A("agg", "PublicadorDevolucao", "OuvinteDevolucao", "ouvintes", "0..*");
        g.A("assoc", "PublicadorDevolucao", "FalhaNotificacao", "fila de reenvio", "0..*");
        g.D("PublicadorDevolucao", "EventoDevolucao", "");
        for (String o : List.of("OuvinteInspecao", "OuvinteMultas", "OuvinteIndicadores", "OuvinteEmail")) g.R(o, "OuvinteDevolucao");
        g.A("assoc", "OuvinteMultas", "ServicoMultas", "[diasAtraso > 0] emitir", "1");
        g.note(g.cde, "try/catch por ouvinte: a falha de um\nnão impede os demais nem desfaz a devolução.", 20, 250, g.n("PublicadorDevolucao"));
    }

    static void r06(IPackage p) throws Exception {
        g.classDiagram(p, "R06 Command — solicitações de circulação");
        g.put("Main", 20, 20);
        g.put("TratadorDeEmprestimo", 330, 20);
        g.put("ExecutorSolicitacoes", 880, 20);
        g.put("SolicitacaoCirculacao", 1000, 230);
        g.put("SolicitacaoReserva", 240, 470);
        g.put("SolicitacaoRenovacao", 760, 470);
        g.putC("CirculacaoFacade", 600, 680);
        g.R("SolicitacaoReserva", "SolicitacaoCirculacao");
        g.R("SolicitacaoRenovacao", "SolicitacaoCirculacao");
        g.D("Main", "SolicitacaoReserva", "cria");
        g.D("Main", "SolicitacaoRenovacao", "cria");
        g.A("assoc", "Main", "TratadorDeEmprestimo", "encaminha", "1");
        g.A("assoc", "TratadorDeEmprestimo", "ExecutorSolicitacoes", "", "1");
        g.D("ExecutorSolicitacoes", "SolicitacaoCirculacao", "executa 1x por UUID (synchronized)");
        g.D("SolicitacaoReserva", "CirculacaoFacade", "reservar()");
        g.D("SolicitacaoRenovacao", "CirculacaoFacade", "renovar()");
    }

    static void r07(IPackage p) throws Exception {
        g.classDiagram(p, "R07 Template Method — rotina comum de cadastro");
        g.put("CadastroAbstrato", 420, 20);
        g.put("CadastroLivro", 20, 360);
        g.put("CadastroPessoa", 300, 360);
        g.put("CadastroEstudante", 580, 360);
        g.put("CadastroAtendente", 880, 360);
        g.put("TratadorDeAcervo", 20, 520);
        g.put("TratadorDePessoa", 300, 520);
        g.put("TratadorDeEstudante", 580, 520);
        g.put("TratadorDeAtendente", 880, 520);
        g.put("iLivro", 40, 660);
        g.put("iPessoa", 320, 660);
        g.put("iEstudante", 600, 660);
        g.put("iAtendente", 900, 660);
        for (String s : List.of("Livro", "Pessoa", "Estudante", "Atendente")) {
            g.G("Cadastro" + s, "CadastroAbstrato");
            String t = s.equals("Livro") ? "TratadorDeAcervo" : "TratadorDe" + s;
            g.A("assoc", t, "Cadastro" + s, "", "1");
            g.R(t, "i" + s);
        }
        g.note(g.cde, "incluir() é final e fixa a ordem:\nbuscar existente -> validar ->\nverificar pré-requisitos -> criar ->\narmazenar -> apresentar,\ntudo numa UnidadeDeTrabalho.", 900, 40, g.n("CadastroAbstrato"));
    }

    static void r08(IPackage p) throws Exception {
        g.classDiagram(p, "R08 Abstract Factory — famílias de persistência");
        g.putC("SistemaIB2B", 20, 40);
        g.put("FabricaPersistencia", 560, 20);
        g.put("FabricaMemoria", 100, 250);
        g.put("FabricaMySQL", 1420, 230);
        g.put("LivroRepositorio", 20, 480);
        g.put("MovimentacaoRepositorio", 380, 480);
        g.put("RelacionamentoRepositorio", 760, 480);
        g.put("UnidadeDeTrabalho", 1000, 330);
        g.R("FabricaMemoria", "FabricaPersistencia");
        g.R("FabricaMySQL", "FabricaPersistencia");
        for (String pr : List.of("LivroRepositorio", "MovimentacaoRepositorio", "RelacionamentoRepositorio", "UnidadeDeTrabalho"))
            g.D("FabricaPersistencia", pr, "cria");
        g.D("SistemaIB2B", "FabricaPersistencia", "escolhe UMA vez (IB2B_PERFIL)");
        g.note(g.cde, "Produtos: classes internas;\nUoW = snapshot + restauração.", 20, 150, g.n("FabricaMemoria"));
        g.note(g.cde, "Os 3 repositórios usam a Connection\nda UoW em curso (commit/rollback).\nOracle: réplica, sem escrita.", 1420, 60, g.n("FabricaMySQL"));
    }

    static void r09(IPackage p) throws Exception {
        g.classDiagram(p, "R09 Decorator — consulta com recursos opcionais");
        g.putC("SistemaIB2B", 20, 40);
        g.put("ConsultaAcervo", 520, 20);
        g.put("LivroInfo", 1060, 20);
        g.put("ConsultaOriginal", 280, 240);
        g.put("DecoradorConsulta", 760, 240);
        g.put("ConsultaComCache", 560, 430);
        g.put("ConsultaComMedicao", 960, 430);
        g.R("ConsultaOriginal", "ConsultaAcervo");
        g.R("DecoradorConsulta", "ConsultaAcervo");
        g.G("ConsultaComCache", "DecoradorConsulta");
        g.G("ConsultaComMedicao", "DecoradorConsulta");
        ILinkPresentation interno = g.A("agg", "DecoradorConsulta", "ConsultaAcervo", "interno", "1");
        INodePresentation dc = g.n("DecoradorConsulta"), ca = g.n("ConsultaAcervo");
        double bx = dc.getLocation().getX() + dc.getWidth() + 40;
        double by = ca.getLocation().getY() + ca.getHeight() + 30;
        double ex = ca.getLocation().getX() + ca.getWidth() - 30;
        interno.setPoints(new java.awt.geom.Point2D[] {
            Astah.pt(dc.getLocation().getX() + dc.getWidth(), dc.getLocation().getY() + dc.getHeight() / 2),
            Astah.pt(bx, dc.getLocation().getY() + dc.getHeight() / 2),
            Astah.pt(bx, by), Astah.pt(ex, by),
            Astah.pt(ex, ca.getLocation().getY() + ca.getHeight()) });
        g.D("ConsultaAcervo", "LivroInfo", "");
        g.D("SistemaIB2B", "ConsultaAcervo", "monta a cadeia (IB2B_CONSULTA)");
        g.note(g.cde, "Guarda só metadados (id, título).\nSe o cache falhar, vira miss e delega.\nA reserva NUNCA usa esta consulta:\na fachada lê o Livro atual.", 120, 430, g.n("ConsultaComCache"));
    }

    static void r10(IPackage p) throws Exception {
        g.classDiagram(p, "R10 Composite — organização do acervo");
        g.put("ElementoAcervo", 330, 20);
        g.put("GrupoAcervo", 120, 220);
        g.put("ItemAcervo", 640, 260);
        g.R("GrupoAcervo", "ElementoAcervo");
        g.R("ItemAcervo", "ElementoAcervo");
        g.A("comp", "GrupoAcervo", "GrupoAcervo", "subgrupos", "0..*");
        g.A("comp", "GrupoAcervo", "ItemAcervo", "itens", "0..*");
        g.note(g.cde, "Recusa ciclo (destino não pode ser ele\nmesmo nem descendente) e livro já\nregistrado em donoDeLivro.\nO item só referencia o id: mover não\ntoca em Livro nem em Movimentacao.", 640, 420, g.n("GrupoAcervo"));
    }

    // =====================================================================
    // 3 — SEQUÊNCIAS
    // =====================================================================

    static void seqR01(IPackage p) throws Exception {
        g.seqDiagram(p, "SEQ R01 State — terceira renovação recusada");
        g.lifeline("T", ":TratadorDeEmprestimo", 40);
        g.lifeline("F", ":CirculacaoFacade", 250);
        g.lifeline("U", ":UnidadeDeTrabalho", 460);
        g.lifeline("L", ":Livro", 650);
        g.lifeline("R", ":Renovado", 820);
        g.lifeline("E", ":Emprestimo", 1010);
        g.msg("T", "F", "renovar(L01, E01, dia)");
        g.msg("F", "U", "executar(operacao)");
        g.msg("F", "L", "renovar(emprestimo)");
        g.msg("L", "R", "renovar(livro, emprestimo)");
        var q = g.msg("R", "E", "numRenovacoes()");
        g.ret(q, "2");
        double y0 = g.y;
        g.y += 35;
        var e1 = g.msg("R", "L", "lancar(RegraNegocioException)");
        var e2 = g.msg("U", "U", "rollback()");
        g.y += 25;
        var a1 = g.msg("R", "E", "prorrogar()");
        var a2 = g.msg("R", "L", "alterarEstado(RENOVADO)");
        var a3 = g.msg("U", "U", "commit()");
        altFragment(new IPresentation[] { e1, e2 }, new IPresentation[] { a1, a2, a3 },
            "numRenovacoes >= 2", "numRenovacoes < 2", 230, y0, 960, g.y - y0 + 20);
        g.note(g.qde, "Guarda falhou: vencimento e\nestado NÃO são alterados.", 1240, y0 + 20, null);
        g.note(g.qde, "prorrogar(): vencimento + 30 dias", 1240, y0 + 200, null);
    }

    static void altFragment(IPresentation[] op1, IPresentation[] op2, String g1, String g2, double x, double y, double w, double h) throws Exception {
        INodePresentation cf = g.qde.createCombinedFragment("", "alt", Astah.pt(x, y), w, h);
        ICombinedFragment m = (ICombinedFragment) cf.getModel();
        m.getInteractionOperands()[0].setGuard(g1);
        m.addInteractionOperand("", g2);
        try {
            g.qde.setInteractionOperandTargets(cf, new IPresentation[][] { op1, op2 });
        } catch (Exception e) {
            System.out.println("aviso operandos: " + e.getMessage());
        }
    }

    static void seqR02(IPackage p) throws Exception {
        g.seqDiagram(p, "SEQ R02 Strategy — multa de 5 dias");
        g.lifeline("O", ":OuvinteMultas", 40);
        g.lifeline("S", ":ServicoMultas", 240);
        g.lifeline("P", ":PoliticaMulta", 440);
        g.lifeline("G", ":GatewayCobranca", 640);
        g.lifeline("M", ":MovimentacaoRepositorio", 840);
        g.msg("O", "S", "emitir(devolucao)");
        var c = g.msg("S", "P", "calcular(5)");
        g.ret(c, "5.00 (MultaOriginal)");
        g.msg("S", "G", "emitir(codigo, 5.00, cpf)");
        g.msg("S", "M", "salvarBoleto(valor, politica.nome())");
        g.note(g.qde, "O boleto grava valor e nome da política:\ntrocar a política depois não recalcula.", 640, g.y + 20, null);
    }

    static void seqR03(IPackage p) throws Exception {
        g.seqDiagram(p, "SEQ R03 Adapter — R$ 7,00 nos bancos A e B");
        g.lifeline("S", ":ServicoMultas", 40);
        g.lifeline("G", ":GatewayCobranca", 230);
        g.lifeline("AA", ":AdaptadorBancoA", 420);
        g.lifeline("A", ":BancoASimulado", 620);
        g.lifeline("AB", ":AdaptadorBancoB", 860);
        g.lifeline("B", ":BancoBSimulado", 1060);
        g.msg("S", "G", "emitir(\"M-L01\", 7.00, cpf) [instalação com banco A]");
        var a = g.msg("G", "AA", "emitir(\"M-L01\", 7.00, cpf)");
        var b = g.msg("AA", "A", "gerarCobranca(\"ML01\", 700, \"12345678901\")");
        g.ret(b, "\"A-ML01\"");
        g.msg("S", "G", "traduzirNotificacao(\"LIQ|A-ML01\")");
        var t2 = g.msg("G", "AA", "traduzirNotificacao(\"LIQ|A-ML01\")");
        g.ret(t2, "NotificacaoPagamento(A-ML01, PAGA)");
        g.msg("S", "S", "quitar()");
        g.y += 20;
        g.msg("S", "G", "emitir(\"M-L01\", 7.00, cpf) [instalação com banco B]");
        var c = g.msg("G", "AB", "emitir(\"M-L01\", 7.00, cpf)");
        var d = g.msg("AB", "B", "criarBoleto(\"123.456.789-01\", 7.00, \"M-L01\")");
        g.ret(d, "\"B/M-L01\"");
        g.note(g.qde, "quitar() devolve false numa\nnotificação repetida: não há\nsegunda quitação.", 40, g.y + 30, null);
    }

    static void seqR04(IPackage p) throws Exception {
        g.seqDiagram(p, "SEQ R04 Facade — efetivar empréstimo");
        g.lifeline("T", ":TratadorDeEmprestimo", 40);
        g.lifeline("F", ":CirculacaoFacade", 250);
        g.lifeline("U", ":UnidadeDeTrabalho", 460);
        g.lifeline("R", ":Relacionamento", 650);
        g.lifeline("M", ":Movimentacao", 830);
        g.lifeline("A", ":Acervo", 1000);
        g.lifeline("L", ":Livro", 1150);
        var c = g.msg("T", "F", "efetivarEmprestimo(L01, E01, dia)");
        g.msg("F", "U", "executar(operacao)");
        g.msg("F", "R", "exigirEstudante(E01)");
        g.msg("F", "M", "exigirReserva(L01, E01)");
        g.msg("F", "A", "exigir(L01)");
        g.msg("F", "L", "emprestar()");
        g.msg("F", "M", "removerReserva(L01)");
        g.msg("F", "M", "emprestar(emprestimo)");
        g.msg("F", "A", "atualizar(livro)");
        g.msg("U", "U", "commit()");
        g.note(g.qde, "Se qualquer passo lançar exceção,\na UnidadeDeTrabalho faz rollback\ne nada fica gravado.", 650, g.y + 20, null);
    }

    static void seqR05(IPackage p) throws Exception {
        g.seqDiagram(p, "SEQ R05 Observer — devolução atrasada");
        g.lifeline("F", ":CirculacaoFacade", 40);
        g.lifeline("U", ":UnidadeDeTrabalho", 240);
        g.lifeline("P", ":PublicadorDevolucao", 440);
        g.lifeline("I", ":OuvinteInspecao", 640);
        g.lifeline("M", ":OuvinteMultas", 820);
        g.lifeline("N", ":OuvinteIndicadores", 1000);
        g.lifeline("E", ":OuvinteEmail", 1190);
        var x = g.msg("F", "U", "executar(devolverEEncerrar)");
        g.ret(x, "commit: livro DEVOLVIDO");
        g.msg("F", "P", "publicar(EventoDevolucao)");
        g.msg("P", "I", "aoDevolver(ev)");
        g.msg("P", "M", "aoDevolver(ev)");
        g.msg("P", "N", "aoDevolver(ev)");
        var e = g.msg("P", "E", "aoDevolver(ev)");
        g.ret(e, "exceção: SMTP fora do ar");
        g.msg("P", "P", "guardarNaFilaDeReenvio(FalhaNotificacao)");
        g.note(g.qde, "OuvinteMultas só emite se diasAtraso > 0.\nA devolução já foi confirmada: falha de ouvinte\nnão a desfaz. O livro fica DEVOLVIDO até a inspeção.", 440, g.y + 20, null);
    }

    static void seqR06(IPackage p) throws Exception {
        g.seqDiagram(p, "SEQ R06 Command — mesma renovação enviada duas vezes");
        g.lifeline("C", ":Main", 40);
        g.lifeline("T", ":TratadorDeEmprestimo", 200);
        g.lifeline("X", ":ExecutorSolicitacoes", 420);
        g.lifeline("S", ":SolicitacaoRenovacao", 640);
        g.lifeline("F", ":CirculacaoFacade", 860);
        var a = g.msg("C", "T", "processar(cmd) [UUID-1, 1ª vez]");
        var b = g.msg("T", "X", "executar(cmd)");
        var c = g.msg("X", "S", "executar(fachada)");
        var d = g.msg("S", "F", "renovar(L01, E01, dia)");
        g.ret(d, "Comprovante");
        g.msg("X", "X", "memorizar(UUID-1, comprovante)");
        var e = g.msg("C", "T", "processar(cmd) [UUID-1 repetido]");
        var f = g.msg("T", "X", "executar(cmd)");
        g.ret(f, "mesmo Comprovante (não executa de novo)");
        g.note(g.qde, "executar() é synchronized: duas submissões\nsimultâneas do mesmo UUID não renovam duas vezes.", 420, g.y + 20, null);
    }

    static void seqR07(IPackage p) throws Exception {
        g.seqDiagram(p, "SEQ R07 Template Method — incluir estudante");
        g.lifeline("T", ":TratadorDeEstudante", 40);
        g.lifeline("C", ":CadastroEstudante", 260);
        g.lifeline("U", ":UnidadeDeTrabalho", 480);
        g.lifeline("R", ":RelacionamentoRepositorio", 690);
        var a = g.msg("T", "C", "incluir(id, cpf)");
        g.msg("C", "U", "executar(passos)");
        g.msg("C", "R", "estudante(id)");
        g.msg("C", "C", "validar(entrada)");
        g.msg("C", "R", "pessoa(cpf)");
        g.msg("C", "C", "criar(entrada)");
        g.msg("C", "R", "salvarEstudante(estudante)");
        g.msg("C", "C", "apresentar(estudante)");
        g.note(g.qde, "Ordem fixa por incluir() (final):\nbuscarExistente, validar, verificarPreRequisitos,\ncriar, armazenar, apresentar.\nCPF inexistente falha em pessoa(cpf),\nantes de qualquer gravação.", 900, 140, null);
    }

    static void seqR08(IPackage p) throws Exception {
        g.seqDiagram(p, "SEQ R08 Abstract Factory — transação compartilhada");
        g.lifeline("S", ":SistemaIB2B", 40);
        g.lifeline("F", ":FabricaMySQL", 230);
        g.lifeline("U", ":UnidadeDeTrabalho", 420);
        g.lifeline("L", ":LivroRepositorio", 620);
        g.lifeline("M", ":MovimentacaoRepositorio", 820);
        g.lifeline("R", ":RelacionamentoRepositorio", 1040);
        g.msg("S", "F", "unidadeDeTrabalho()");
        g.msg("S", "F", "livros()");
        g.msg("S", "F", "movimentacao()");
        g.msg("S", "F", "relacionamento()");
        g.msg("S", "U", "executar(operacao)");
        g.msg("U", "U", "abrirConexao()");
        g.msg("U", "L", "salvar(livro)");
        g.msg("U", "R", "salvarPessoa(pessoa)");
        g.msg("U", "M", "salvarReserva(reserva)");
        g.msg("U", "U", "commit()");
        g.note(g.qde, "A família é escolhida UMA vez (IB2B_PERFIL).\nOs três repositórios usam a MESMA Connection\nda unidade de trabalho; falha => rollback.", 620, g.y + 20, null);
    }

    static void seqR09(IPackage p) throws Exception {
        g.seqDiagram(p, "SEQ R09 Decorator — cache de metadados e reserva");
        g.lifeline("T", ":TratadorDeAcervo", 40);
        g.lifeline("M", ":ConsultaComMedicao", 240);
        g.lifeline("C", ":ConsultaComCache", 450);
        g.lifeline("O", ":ConsultaOriginal", 650);
        g.lifeline("R", ":LivroRepositorio", 840);
        g.lifeline("F", ":CirculacaoFacade", 1040);
        var a = g.msg("T", "M", "consultar(L01) [1ª consulta]");
        var b = g.msg("M", "C", "consultar(L01)");
        var c = g.msg("C", "O", "consultar(L01) [miss]");
        g.msg("O", "R", "buscar(L01)");
        g.msg("C", "C", "guardar(L01, info)");
        g.msg("M", "M", "contarChamada()");
        var d = g.msg("T", "M", "consultar(L01) [2ª consulta]");
        var e = g.msg("M", "C", "consultar(L01)");
        g.ret(e, "LivroInfo (hit, sem repositório)");
        g.msg("M", "M", "contarChamada()");
        g.msg("F", "R", "buscar(L01) [reserva: estado ATUAL]");
        g.note(g.qde, "O cache não participa da reserva:\nlivro não LIVRE => reserva recusada.", 1100, g.y + 10, null);
    }

    static void seqR10(IPackage p) throws Exception {
        g.seqDiagram(p, "SEQ R10 Composite — contagem, movimento e ciclo");
        g.lifeline("C", "Cliente", 40);
        g.lifeline("U", "unidade:GrupoAcervo", 220);
        g.lifeline("S1", "s1:GrupoAcervo", 460);
        g.lifeline("S2", "s2:GrupoAcervo", 680);
        var a = g.msg("C", "U", "contar()");
        var b = g.msg("U", "S1", "contar()");
        g.ret(b, "3");
        var c = g.msg("U", "S2", "contar()");
        g.ret(c, "2");
        g.msg("C", "S1", "transferirLivro(\"L1\", s2)");
        g.msg("S1", "S1", "removerItem(\"L1\")");
        g.msg("S1", "S2", "receberItem(ItemAcervo L1)");
        g.msg("S1", "S1", "atualizarDono(\"L1\", s2)");
        var d = g.msg("C", "S1", "anexarGrupo(unidade)");
        g.ret(d, "RegraNegocioException: Ciclo no acervo");
        g.note(g.qde, "unidade.contar() = 3 + 2 = 5.\nNa transferência só a referência (id) muda:\nLivro e Emprestimo não são tocados.", 880, 300, null);
    }

    // =====================================================================
    // 4 — IMPLANTAÇÃO
    // =====================================================================

    static void implantacaoTipos(IPackage p) throws Exception {
        g.deploymentDiagram(p, "11 Implantação — tipos");
        INode comp = g.be.createNode(p, "Computador");
        INode jvm = g.be.createNode(p, "JVM Java 17+");
        IArtifact jar = g.be.createArtifact(p, "ib2b-ra03-gof.jar");
        IArtifact sim = g.be.createArtifact(p, "BancoASimulado / BancoBSimulado");
        INode srv = g.be.createNode(p, "Servidor de banco de dados");
        INode mysql = g.be.createNode(p, "MySQL 8.x / InnoDB");
        IArtifact schema = g.be.createArtifact(p, "ib2b (schema)");
        INode banco = g.be.createNode(p, "API do banco real");
        banco.addStereotype("externo, não implementado");
        var pComp = g.putEl("comp", comp, null, 40, 40);
        var pJvm = g.putEl("jvm", jvm, pComp, 60, 90);
        var pJar = g.putEl("jar", jar, pJvm, 80, 140);
        var pSim = g.putEl("sim", sim, pJvm, 80, 230);
        var pSrv = g.putEl("srv", srv, null, 620, 40);
        var pMy = g.putEl("mysql", mysql, pSrv, 640, 90);
        g.putEl("schema", schema, pMy, 660, 140);
        var pBanco = g.putEl("banco", banco, null, 80, 470);
        g.cde.createLinkPresentation(g.be.createDependency(sim, jar, "GatewayCobranca (Adapter, chamada local)"), pJar, pSim);
        g.cde.createLinkPresentation(g.be.createAssociation(jvm, mysql, "JDBC / TCP 3306 (IB2B_PERFIL=mysql)", "", ""), pJvm, pMy);
        g.cde.createLinkPresentation(g.be.createDependency(banco, sim, "substituiria em produção"), pSim, pBanco);
        g.note(g.cde, "Perfil de TESTE: família em memória,\nsem rede. Bancos A e B são SIMULADOS\ndentro da JVM.", 420, 330, pComp);
        g.note(g.cde, "Oracle do RA02: réplica externa,\nnão recebe escritas da aplicação.", 720, 330, pSrv);
    }

    static void implantacaoInstancias(IPackage p) throws Exception {
        g.deploymentDiagram(p, "12 Implantação — instâncias");
        INode comp = (INode) find(INode.class, "Computador");
        INode jvm = (INode) find(INode.class, "JVM Java 17+");
        INode srv = (INode) find(INode.class, "Servidor de banco de dados");
        INode mysql = (INode) find(INode.class, "MySQL 8.x / InnoDB");
        var nb = g.nodeInstance("nb", "notebook01", comp, null, 40, 40);
        var jv = g.nodeInstance("jvm", "jvm01", jvm, nb, 60, 90);
        IArtifact jar = (IArtifact) find(IArtifact.class, "ib2b-ra03-gof.jar");
        IArtifact simA = g.be.createArtifact(p, "bancoA : BancoASimulado");
        var pJar = g.putEl("jar", jar, jv, 80, 150);
        var pSim = g.putEl("simA", simA, jv, 80, 240);
        var dk = g.nodeInstance("dk", "docker01", srv, null, 620, 40);
        var ms = g.nodeInstance("ms", "mysql01", mysql, dk, 640, 90);
        IArtifact schema = (IArtifact) find(IArtifact.class, "ib2b (schema)");
        g.putEl("schema", schema, ms, 660, 150);
        g.cde.createLinkPresentation(g.be.createDependency(simA, jar, "AdaptadorBancoA (IB2B_BANCO=A)"), pJar, pSim);
        try {
            g.cde.createInstanceSpecificationLink(jv, ms);
        } catch (Exception e) {
            g.cde.createLinkPresentation(g.be.createDependency(ms.getModel() instanceof INamedElement n2 ? n2 : null,
                (INamedElement) jv.getModel(), "JDBC / TCP 3306"), jv, ms);
        }
        g.note(g.cde, "Instâncias de EXEMPLO (docker-compose.yml),\nnão servidores já provisionados.\nLigação jvm01 — mysql01: JDBC / TCP 3306.", 620, 360, dk);
    }

    static INamedElement find(Class<?> type, String name) throws Exception {
        for (INamedElement e : g.pa.findElements(type, name)) if (e.getFullName("/").contains("RA03")) return e;
        throw new IllegalStateException("não achei " + name);
    }
}
