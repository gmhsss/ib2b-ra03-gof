package br.edu.pucpr.ib2b.estado;

import br.edu.pucpr.ib2b.dominio.*;

public final class Reservado implements EstadoDoLivro {

    public EstadoLivro tipo() {
        return EstadoLivro.RESERVADO;
    }

    public void emprestar(Livro l) {
        l.alterarEstado(EstadoLivro.EMPRESTADO);
    }
}
