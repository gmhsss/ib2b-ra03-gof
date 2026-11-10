package br.edu.pucpr.ib2b.persistencia;

import java.util.function.Supplier;

public interface UnidadeDeTrabalho {
    <T> T executar(Supplier<T> operacao);
}
