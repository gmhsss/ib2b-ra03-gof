package br.edu.pucpr.ib2b.padroes;

import br.edu.pucpr.ib2b.dominio.*;
import br.edu.pucpr.ib2b.servico.*;
import java.util.*;

/** A exclusão mútua protege o teste-mais-execução contra envios simultâneos. */
public final class ExecutorSolicitacoes {

    private final Map<UUID, Comprovante> feitos = new HashMap<>();
    private final CirculacaoFacade fachada;

    public ExecutorSolicitacoes(CirculacaoFacade fachada) {
        this.fachada = fachada;
    }

    public synchronized Comprovante executar(SolicitacaoCirculacao comando) {
        return feitos.computeIfAbsent(comando.identificador(), ignorado -> comando.executar(fachada));
    }

    public synchronized int totalUnicos() {
        return feitos.size();
    }
}
