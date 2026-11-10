package br.edu.pucpr.ib2b.padroes;

import br.edu.pucpr.ib2b.dominio.*;
import java.time.LocalDate;

/**
 * Evento imutável (modelo push): já leva vencimento, data de devolução e estudante,
 * para o ouvinte não precisar consultar o banco depois (quando os dados já podem ter mudado).
 */
public record EventoDevolucao(Devolucao devolucao) {
    public String idLivro() {
        return devolucao.idLivro();
    }

    public String idEstudante() {
        return devolucao.idEstudante();
    }

    public LocalDate vencimento() {
        return devolucao.vencimento();
    }

    public LocalDate dataDevolucao() {
        return devolucao.dataDevolucao();
    }

    public long diasAtraso() {
        return devolucao.diasAtraso();
    }
}
