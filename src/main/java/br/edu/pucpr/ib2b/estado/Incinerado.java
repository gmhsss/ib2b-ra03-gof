package br.edu.pucpr.ib2b.estado;

import br.edu.pucpr.ib2b.dominio.*;

public final class Incinerado implements EstadoDoLivro {

    public EstadoLivro tipo() {
        return EstadoLivro.INCINERADO;
    }
}
