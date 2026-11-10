package br.edu.pucpr.ib2b;

import br.edu.pucpr.ib2b.persistencia.*;
import java.lang.reflect.Proxy;
import java.util.*;

/** Fábrica de teste que embrulha a família em memória e conta toda chamada a salvar... e remover... */
final class EspiaDeGravacoes implements FabricaPersistencia {

    final FabricaMemoria real = new FabricaMemoria();
    final List<String> gravacoes = new ArrayList<>();

    @SuppressWarnings("unchecked")
    private <T> T espiar(T alvo, Class<T> tipo) {
        return (T) Proxy.newProxyInstance(
            getClass().getClassLoader(),
            new Class<?>[] { tipo },
            (p, m, args) -> {
                if (m.getName().startsWith("salvar") || m.getName().startsWith("remover")) gravacoes.add(
                    m.getName()
                );
                return m.invoke(alvo, args);
            }
        );
    }

    public LivroRepositorio livros() {
        return espiar(real.livros(), LivroRepositorio.class);
    }

    public MovimentacaoRepositorio movimentacao() {
        return espiar(real.movimentacao(), MovimentacaoRepositorio.class);
    }

    public RelacionamentoRepositorio relacionamento() {
        return espiar(real.relacionamento(), RelacionamentoRepositorio.class);
    }

    public UnidadeDeTrabalho unidadeDeTrabalho() {
        return real.unidadeDeTrabalho();
    }

    public String familia() {
        return "MEMORIA(espiã)";
    }
}
