package br.edu.pucpr.ib2b.dominio;

import java.time.*;
import java.time.temporal.ChronoUnit;

public record Devolucao(String idLivro, String idEstudante, LocalDate vencimento, LocalDate dataDevolucao) {
    public long diasAtraso() {
        return Math.max(0, ChronoUnit.DAYS.between(vencimento, dataDevolucao));
    }
}
