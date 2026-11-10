package br.edu.pucpr.ib2b.servico;

import br.edu.pucpr.ib2b.dominio.*;
import java.time.*;
import java.util.*;

/** Tratador: só delega. A devolução publica o evento (Observer) dentro da fachada, após o commit. */
public final class TratadorDeDevolucao implements iDevolucao {

    private final CirculacaoFacade fachada;
    private final ServicoMultas multas;

    public TratadorDeDevolucao(CirculacaoFacade fachada, ServicoMultas multas) {
        this.fachada = fachada;
        this.multas = multas;
    }

    public Comprovante registrarDevolucao(String livro, String estudante, LocalDate dia) {
        return fachada.devolver(livro, estudante, dia);
    }

    /** Emissão manual/idempotente: se o ouvinte de multa já emitiu, devolve o mesmo boleto. */
    public Optional<BoletoMulta> emitirBoletoMulta(String livro, String estudante) {
        return multas.emitir(fachada.exigirDevolucao(livro, estudante));
    }

    public Optional<BoletoMulta> consultarMulta(String livro, String estudante) {
        return multas.multaDa(fachada.exigirDevolucao(livro, estudante));
    }

    public boolean registrarPagamento(String notificacaoDoBanco) {
        return multas.registrarNotificacao(notificacaoDoBanco);
    }

    public Comprovante inspecionar(String livro, boolean apto, LocalDate dia) {
        return fachada.inspecionar(livro, apto, dia);
    }

    public Comprovante concluirReparo(String livro, LocalDate dia) {
        return fachada.concluirReparo(livro, dia);
    }

    public Comprovante laudoIrrecuperavel(String livro, LocalDate dia) {
        return fachada.laudoIrrecuperavel(livro, dia);
    }
}
