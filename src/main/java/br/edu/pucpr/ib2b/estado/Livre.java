package br.edu.pucpr.ib2b.estado;

import br.edu.pucpr.ib2b.dominio.*;

public final class Livre implements EstadoDoLivro {

    public EstadoLivro tipo() {
        return EstadoLivro.LIVRE;
    }

    public void reservar(Livro l) {
        l.alterarEstado(EstadoLivro.RESERVADO);
    }

    public void verificarExclusao(Livro l) {
        // permitido: livro LIVRE não tem reserva nem empréstimo pendente
    }

    public void encaminharParaReparo(Livro l) {
        l.alterarEstado(EstadoLivro.REPARANDO);
    }
}
