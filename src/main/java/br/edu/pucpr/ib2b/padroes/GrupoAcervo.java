package br.edu.pucpr.ib2b.padroes;

import br.edu.pucpr.ib2b.dominio.*;
import java.util.*;

/**
 * Composite — grupo (unidade, seção, subseção) de profundidade variável.
 * Regras: (1) sem ciclos; (2) um livro pertence a no máximo UM grupo da árvore.
 * A árvore guarda só IDs: mover um item nunca toca em Livro nem em Movimentacao.
 */
public final class GrupoAcervo implements ElementoAcervo {

    private final String nome;
    private final Map<String, GrupoAcervo> grupos = new LinkedHashMap<>();
    private final Map<String, ItemAcervo> itens = new LinkedHashMap<>();
    /** Registro compartilhado por todos os grupos da mesma árvore: idLivro -> grupo dono. */
    private Map<String, GrupoAcervo> donoDeLivro;
    private GrupoAcervo pai;

    public GrupoAcervo(String nome) {
        this(nome, new HashMap<>());
    }

    private GrupoAcervo(String nome, Map<String, GrupoAcervo> registro) {
        if (nome == null || nome.isBlank()) throw new RegraNegocioException("Nome do grupo obrigatório");
        this.nome = nome;
        this.donoDeLivro = registro;
    }

    public String nome() {
        return nome;
    }

    // ---------- operações comuns a grupo e item (Component) ----------

    @Override
    public int contar() {
        int total = itens.size();
        for (var g : grupos.values()) total += g.contar(); // recursão: percorre a árvore
        return total;
    }

    @Override
    public List<String> listarIds() {
        List<String> ids = new ArrayList<>(itens.keySet());
        for (var g : grupos.values()) ids.addAll(g.listarIds());
        return List.copyOf(ids);
    }

    // ---------- gestão de filhos ----------

    public GrupoAcervo novaSecao(String nomeSecao) {
        exigirNomeLivre(nomeSecao);
        var g = new GrupoAcervo(nomeSecao, donoDeLivro);
        g.pai = this;
        grupos.put(nomeSecao, g);
        return g;
    }

    /** Anexa um grupo raiz (pode vir de outra árvore, com seus livros). */
    public void anexarGrupo(GrupoAcervo outro) {
        Objects.requireNonNull(outro);
        if (outro == this || outro.ehAncestralDe(this)) throw new RegraNegocioException("Ciclo no acervo");
        if (outro.pai != null) throw new RegraNegocioException("Grupo já pertence a outro grupo");
        exigirNomeLivre(outro.nome);
        if (outro.donoDeLivro != donoDeLivro) {
            for (String id : outro.listarIds()) if (
                donoDeLivro.containsKey(id)
            ) throw new RegraNegocioException("Livro já pertence a outro grupo: " + id);
            donoDeLivro.putAll(outro.donoDeLivro);
            outro.adotarRegistro(donoDeLivro);
        }
        outro.pai = this;
        grupos.put(outro.nome, outro);
    }

    /** Move ESTE grupo (com tudo dentro) para baixo de outro grupo da mesma árvore. */
    public void moverPara(GrupoAcervo destino) {
        Objects.requireNonNull(destino);
        if (destino == this || ehAncestralDe(destino)) throw new RegraNegocioException("Ciclo no acervo");
        if (destino.donoDeLivro != donoDeLivro) throw new RegraNegocioException(
            "Grupos de árvores diferentes"
        );
        if (destino == pai) return;
        destino.exigirNomeLivre(nome);
        if (pai != null) pai.grupos.remove(nome);
        pai = destino;
        destino.grupos.put(nome, this);
    }

    public void incluirLivro(String idLivro) {
        if (donoDeLivro.containsKey(idLivro)) throw new RegraNegocioException(
            "Livro já pertence a um grupo: " + idLivro
        );
        itens.put(idLivro, new ItemAcervo(idLivro));
        donoDeLivro.put(idLivro, this);
    }

    public void removerLivro(String idLivro) {
        if (donoDeLivro.get(idLivro) != this) throw new RegraNegocioException("Livro não está neste grupo");
        itens.remove(idLivro);
        donoDeLivro.remove(idLivro);
    }

    /** Move só a REFERÊNCIA (ItemAcervo); o Livro e o empréstimo não são tocados. */
    public void transferirLivro(String idLivro, GrupoAcervo destino) {
        if (destino.donoDeLivro != donoDeLivro) throw new RegraNegocioException(
            "Grupos de árvores diferentes"
        );
        if (donoDeLivro.get(idLivro) != this) throw new RegraNegocioException(
            "Livro não pertence a este grupo"
        );
        if (destino == this) return;
        destino.itens.put(idLivro, itens.remove(idLivro));
        donoDeLivro.put(idLivro, destino);
    }

    // ---------- auxiliares ----------

    private boolean ehAncestralDe(GrupoAcervo g) {
        for (var n = g; n != null; n = n.pai) if (n == this) return true;
        return false;
    }

    private void exigirNomeLivre(String nomeFilho) {
        if (grupos.containsKey(nomeFilho)) throw new RegraNegocioException(
            "Nome de seção duplicado: " + nomeFilho
        );
    }

    private void adotarRegistro(Map<String, GrupoAcervo> registro) {
        donoDeLivro = registro;
        for (var g : grupos.values()) g.adotarRegistro(registro);
    }
}
