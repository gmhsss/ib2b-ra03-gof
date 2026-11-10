package br.edu.pucpr.ib2b.estado;

import br.edu.pucpr.ib2b.dominio.*;

public final class Devolvido implements EstadoDoLivro {

    public EstadoLivro tipo() {
        return EstadoLivro.DEVOLVIDO;
    }

    public void inspecionar(Livro l, boolean apto) {
        l.alterarEstado(apto ? EstadoLivro.LIVRE : EstadoLivro.REPARANDO);
    }
}
