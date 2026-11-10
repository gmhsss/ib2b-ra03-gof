package br.edu.pucpr.ib2b.estado;

import br.edu.pucpr.ib2b.dominio.*;

public final class Estados {

    private Estados() {}

    public static EstadoDoLivro doTipo(EstadoLivro t) {
        return switch (t) {
            case LIVRE -> new Livre();
            case RESERVADO -> new Reservado();
            case EMPRESTADO -> new Emprestado();
            case RENOVADO -> new Renovado();
            case DEVOLVIDO -> new Devolvido();
            case REPARANDO -> new Reparando();
            case INCINERADO -> new Incinerado();
        };
    }
}
