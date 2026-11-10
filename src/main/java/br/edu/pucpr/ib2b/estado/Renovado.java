package br.edu.pucpr.ib2b.estado;

import br.edu.pucpr.ib2b.dominio.*;

public final class Renovado implements EstadoDoLivro {

    public EstadoLivro tipo() {
        return EstadoLivro.RENOVADO;
    }

    /** Transição RENOVADO --renovar [numRenovacoes < 2] / prorrogar 30 dias--> RENOVADO. */
    public void renovar(Livro l, Emprestimo e) {
        // guarda: avaliada ANTES de qualquer alteração; se falhar, vencimento e estado ficam intactos
        if (e.numRenovacoes() >= Emprestimo.LIMITE_RENOVACOES) {
            throw new RegraNegocioException(
                "Limite de " + Emprestimo.LIMITE_RENOVACOES + " renovações atingido"
            );
        }
        // ação: prorrogar o vencimento em 30 dias
        e.prorrogar();
        l.alterarEstado(EstadoLivro.RENOVADO);
    }

    public void devolver(Livro l) {
        l.alterarEstado(EstadoLivro.DEVOLVIDO);
    }
}
