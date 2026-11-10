package br.edu.pucpr.ib2b.padroes;

import java.util.*;
import java.util.concurrent.atomic.*;

/** Decorator — medição: conta chamadas e soma o tempo gasto pela cadeia interna. */
public final class ConsultaComMedicao extends DecoradorConsulta {

    private final AtomicInteger chamadas = new AtomicInteger();
    private final AtomicLong nanosAcumulados = new AtomicLong();

    public ConsultaComMedicao(ConsultaAcervo interno) {
        super(interno);
    }

    @Override
    public Optional<LivroInfo> consultar(String id) {
        long inicio = System.nanoTime();
        try {
            return interno.consultar(id);
        } finally {
            chamadas.incrementAndGet();
            nanosAcumulados.addAndGet(System.nanoTime() - inicio);
        }
    }

    public int chamadas() {
        return chamadas.get();
    }

    public long nanosAcumulados() {
        return nanosAcumulados.get();
    }
}
