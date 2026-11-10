package br.edu.pucpr.ib2b.servico;

import br.edu.pucpr.ib2b.dominio.*;
import br.edu.pucpr.ib2b.persistencia.*;
import java.util.Optional;

public final class Movimentacao {

    private final MovimentacaoRepositorio repo;

    public Movimentacao(MovimentacaoRepositorio repo) {
        this.repo = repo;
    }

    public Reserva exigirReserva(String livro, String estudante) {
        var r = repo.reserva(livro).orElseThrow(() -> new RegraNegocioException("Reserva não encontrada"));
        if (!r.idEstudante().equals(estudante)) throw new RegraNegocioException(
            "Reserva pertence a outro estudante"
        );
        return r;
    }

    public Emprestimo exigirEmprestimo(String livro, String estudante) {
        var e = repo
            .emprestimo(livro)
            .orElseThrow(() -> new RegraNegocioException("Empréstimo não encontrado"));
        if (!e.idEstudante().equals(estudante) || e.encerrado()) throw new RegraNegocioException(
            "Empréstimo não ativo para o estudante"
        );
        return e;
    }

    public Optional<Reserva> reservaAtiva(String livro) {
        return repo.reserva(livro);
    }

    public Devolucao exigirDevolucao(String livro, String estudante) {
        var d = repo
            .devolucao(livro)
            .orElseThrow(() -> new RegraNegocioException("Devolução não encontrada"));
        if (!d.idEstudante().equals(estudante)) throw new RegraNegocioException(
            "Estudante não corresponde à devolução"
        );
        return d;
    }

    public void reservar(Reserva r) {
        repo.salvarReserva(r);
    }

    public void emprestar(Emprestimo e) {
        repo.salvarEmprestimo(e);
    }

    public void removerReserva(String livro) {
        repo.removerReserva(livro);
    }

    public void devolver(Devolucao d) {
        repo.salvarDevolucao(d);
    }
}
