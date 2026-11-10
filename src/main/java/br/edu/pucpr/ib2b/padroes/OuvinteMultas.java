package br.edu.pucpr.ib2b.padroes;

import br.edu.pucpr.ib2b.servico.*;

public final class OuvinteMultas implements OuvinteDevolucao {

    private final ServicoMultas servico;

    public OuvinteMultas(ServicoMultas servico) {
        this.servico = servico;
    }

    public void aoDevolver(EventoDevolucao ev) {
        if (ev.diasAtraso() > 0) servico.emitir(ev.devolucao()); // no prazo: nenhuma multa
    }
}
