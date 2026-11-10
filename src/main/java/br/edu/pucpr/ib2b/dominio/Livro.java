package br.edu.pucpr.ib2b.dominio;

import br.edu.pucpr.ib2b.estado.*;

public final class Livro {

    private final String id, titulo;
    private EstadoDoLivro estado;

    public Livro(String id, String titulo) {
        this(id, titulo, EstadoLivro.LIVRE);
    }

    public Livro(String id, String titulo, EstadoLivro estado) {
        if (id == null || id.isBlank() || titulo == null || titulo.isBlank()) throw new RegraNegocioException(
            "Livro inválido"
        );
        this.id = id;
        this.titulo = titulo;
        this.estado = Estados.doTipo(estado);
    }

    public String id() {
        return id;
    }

    public String titulo() {
        return titulo;
    }

    public EstadoLivro estado() {
        return estado.tipo();
    }

    /** Usado pelos estados concretos para efetivar uma transição (e na reconstituição da persistência). */
    public void alterarEstado(EstadoLivro novo) {
        estado = Estados.doTipo(novo);
    }

    public void reservar() {
        estado.reservar(this);
    }

    public void emprestar() {
        estado.emprestar(this);
    }

    public void renovar(Emprestimo e) {
        estado.renovar(this, e);
    }

    public void devolver() {
        estado.devolver(this);
    }

    public void inspecionar(boolean apto) {
        estado.inspecionar(this, apto);
    }

    public void encaminharParaReparo() {
        estado.encaminharParaReparo(this);
    }

    public void concluirReparo() {
        estado.concluirReparo(this);
    }

    public void laudoIrrecuperavel() {
        estado.laudoIrrecuperavel(this);
    }

    public void verificarExclusao() {
        estado.verificarExclusao(this);
    }

    public Livro copia() {
        return new Livro(id, titulo, estado());
    }
}
