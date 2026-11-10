package br.edu.pucpr.ib2b.padroes;

import java.util.*;

public abstract class DecoradorConsulta implements ConsultaAcervo {

    protected final ConsultaAcervo interno;

    protected DecoradorConsulta(ConsultaAcervo interno) {
        this.interno = Objects.requireNonNull(interno);
    }

    public Optional<LivroInfo> consultar(String id) {
        return interno.consultar(id);
    }

    /** Repassa a invalidação para o próximo elo da cadeia. */
    @Override
    public void invalidar(String id) {
        interno.invalidar(id);
    }
}
