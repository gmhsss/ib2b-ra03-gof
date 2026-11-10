package br.edu.pucpr.ib2b.estado;

import br.edu.pucpr.ib2b.dominio.*;

public final class Reparando implements EstadoDoLivro {

    public EstadoLivro tipo() {
        return EstadoLivro.REPARANDO;
    }

    public void concluirReparo(Livro l) {
        l.alterarEstado(EstadoLivro.LIVRE);
    }

    public void laudoIrrecuperavel(Livro l) {
        l.alterarEstado(EstadoLivro.INCINERADO);
    }
}
