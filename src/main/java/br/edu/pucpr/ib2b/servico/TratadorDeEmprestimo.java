package br.edu.pucpr.ib2b.servico;

import br.edu.pucpr.ib2b.dominio.*;
import br.edu.pucpr.ib2b.padroes.*;
import java.time.*;
import java.util.*;

/** Tratador: delega à fachada; reserva e renovação viram Command e passam pelo executor. */
public final class TratadorDeEmprestimo implements iEmprestimo {

    private final CirculacaoFacade fachada;
    private final ExecutorSolicitacoes executor;

    public TratadorDeEmprestimo(CirculacaoFacade fachada) {
        this.fachada = fachada;
        this.executor = new ExecutorSolicitacoes(fachada);
    }

    /** A tela cria a solicitação (com UUID, autor e instante) e a entrega aqui. */
    public Comprovante processar(SolicitacaoCirculacao comando) {
        return executor.executar(comando);
    }

    public Comprovante solicitarReserva(String livro, String estudante, LocalDate dia) {
        return processar(new SolicitacaoReserva(UUID.randomUUID(), estudante, Instant.now(), livro, dia));
    }

    public Optional<Reserva> consultarReservas(String livro) {
        return fachada.reservaAtiva(livro);
    }

    public Comprovante efetivarEmprestimo(String livro, String estudante, LocalDate dia) {
        return fachada.efetivarEmprestimo(livro, estudante, dia);
    }

    public Comprovante renovarEmprestimo(String livro, String estudante, LocalDate dia) {
        return processar(new SolicitacaoRenovacao(UUID.randomUUID(), estudante, Instant.now(), livro, dia));
    }
}
