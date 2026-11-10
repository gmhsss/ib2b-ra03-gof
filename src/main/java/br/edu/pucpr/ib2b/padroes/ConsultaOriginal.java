package br.edu.pucpr.ib2b.padroes;

import br.edu.pucpr.ib2b.persistencia.*;
import java.util.*;

/** Decorator — ConcreteComponent: consulta original, sem cache nem medição. */
public final class ConsultaOriginal implements ConsultaAcervo {

    private final LivroRepositorio repo;

    public ConsultaOriginal(LivroRepositorio repo) {
        this.repo = repo;
    }

    public Optional<LivroInfo> consultar(String id) {
        return repo.buscar(id).map(l -> new LivroInfo(l.id(), l.titulo()));
    }
}
