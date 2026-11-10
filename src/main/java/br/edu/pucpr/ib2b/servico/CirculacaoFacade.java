package br.edu.pucpr.ib2b.servico;

import br.edu.pucpr.ib2b.dominio.*;
import br.edu.pucpr.ib2b.padroes.*;
import br.edu.pucpr.ib2b.persistencia.*;
import java.time.*;
import java.util.*;

/**
 * Facade: entrada única da circulação. Coordena Relacionamento, Movimentacao, Acervo e Livro
 * dentro de UMA unidade de trabalho por operação e devolve Comprovante (nunca coleções).
 * Os tratadores delegam para cá; a CLI só conhece os tratadores.
 */
public final class CirculacaoFacade {

    private final UnidadeDeTrabalho uow;
    private final Acervo acervo;
    private final Relacionamento relacionamento;
    private final Movimentacao movimentacao;
    private final PublicadorDevolucao publicador;
    private final Runnable aposRemocaoReserva;

    public CirculacaoFacade(FabricaPersistencia f, PublicadorDevolucao p) {
        this(f, p, () -> {});
    }

    /** Gancho usado para comprovar rollback quando há falha depois da remoção da reserva. */
    public CirculacaoFacade(FabricaPersistencia f, PublicadorDevolucao p, Runnable gancho) {
        this.uow = f.unidadeDeTrabalho();
        this.acervo = new Acervo(f.livros());
        this.relacionamento = new Relacionamento(f.relacionamento());
        this.movimentacao = new Movimentacao(f.movimentacao());
        this.publicador = p;
        this.aposRemocaoReserva = gancho;
    }

    public Comprovante reservar(String livro, String estudante, LocalDate hoje) {
        return uow.executar(() -> {
            relacionamento.exigirEstudante(estudante);
            var l = acervo.exigir(livro);
            l.reservar();
            movimentacao.reservar(new Reserva(livro, estudante, hoje));
            acervo.atualizar(l);
            return new Comprovante("RESERVA", livro, estudante, hoje, "Confirmada");
        });
    }

    public Comprovante efetivarEmprestimo(String livro, String estudante, LocalDate hoje) {
        return uow.executar(() -> {
            relacionamento.exigirEstudante(estudante); // 1. estudante existe
            movimentacao.exigirReserva(livro, estudante); // 2. reserva é deste estudante
            var l = acervo.exigir(livro);
            l.emprestar(); // 3. State: só RESERVADO aceita
            movimentacao.removerReserva(livro); // 4. remove a reserva ativa
            aposRemocaoReserva.run(); //    (gancho para o teste injetar falha aqui)
            var e = new Emprestimo(livro, estudante, hoje);
            movimentacao.emprestar(e); // 5. cria o empréstimo
            acervo.atualizar(l); // 6. grava o novo estado do livro
            // qualquer exceção nos passos 1-6 => UnidadeDeTrabalho faz rollback de tudo
            return new Comprovante("EMPRESTIMO", livro, estudante, hoje, "Vence em " + e.vencimento());
        });
    }

    public Comprovante renovar(String livro, String estudante, LocalDate hoje) {
        return uow.executar(() -> {
            var e = movimentacao.exigirEmprestimo(livro, estudante);
            var l = acervo.exigir(livro);
            l.renovar(e);
            movimentacao.emprestar(e);
            acervo.atualizar(l);
            return new Comprovante("RENOVACAO", livro, estudante, hoje, "Novo vencimento: " + e.vencimento());
        });
    }

    public Comprovante devolver(String livro, String estudante, LocalDate hoje) {
        Devolucao d = uow.executar(() -> {
            var e = movimentacao.exigirEmprestimo(livro, estudante);
            var l = acervo.exigir(livro);
            l.devolver();
            e.encerrar();
            movimentacao.emprestar(e);
            acervo.atualizar(l);
            var registro = new Devolucao(livro, estudante, e.vencimento(), hoje);
            movimentacao.devolver(registro);
            return registro;
        });
        // Após COMMIT: falha de listener não desfaz a devolução. O livro aguarda inspeção.
        publicador.publicar(new EventoDevolucao(d));
        return new Comprovante("DEVOLUCAO", livro, estudante, hoje, "Dias de atraso: " + d.diasAtraso());
    }

    public Comprovante inspecionar(String livro, boolean bom, LocalDate hoje) {
        return uow.executar(() -> {
            var l = acervo.exigir(livro);
            l.inspecionar(bom);
            acervo.atualizar(l);
            return new Comprovante("INSPECAO", livro, "-", hoje, "Novo estado: " + l.estado());
        });
    }

    public Comprovante encaminharParaReparo(String livro, LocalDate hoje) {
        return uow.executar(() -> {
            var l = acervo.exigir(livro);
            l.encaminharParaReparo();
            acervo.atualizar(l);
            return new Comprovante("ENCAMINHAR_REPARO", livro, "-", hoje, "REPARANDO");
        });
    }

    public Comprovante concluirReparo(String livro, LocalDate hoje) {
        return uow.executar(() -> {
            var l = acervo.exigir(livro);
            l.concluirReparo();
            acervo.atualizar(l);
            return new Comprovante("REPARO", livro, "-", hoje, "Concluído");
        });
    }

    public Comprovante laudoIrrecuperavel(String livro, LocalDate hoje) {
        return uow.executar(() -> {
            var l = acervo.exigir(livro);
            l.laudoIrrecuperavel();
            acervo.atualizar(l);
            return new Comprovante("DESCARTE", livro, "-", hoje, "INCINERADO");
        });
    }

    /** Consulta para os tratadores: devolve a reserva ativa (ou vazio), nunca a coleção. */
    public Optional<Reserva> reservaAtiva(String livro) {
        return movimentacao.reservaAtiva(livro);
    }

    public Devolucao exigirDevolucao(String livro, String estudante) {
        return movimentacao.exigirDevolucao(livro, estudante);
    }
}
