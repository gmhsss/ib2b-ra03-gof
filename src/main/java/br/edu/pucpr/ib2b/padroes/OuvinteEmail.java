package br.edu.pucpr.ib2b.padroes;

import java.util.function.Consumer;

public final class OuvinteEmail implements OuvinteDevolucao {

    private final Consumer<String> envio;

    public OuvinteEmail(Consumer<String> envio) {
        this.envio = envio;
    }

    public void aoDevolver(EventoDevolucao ev) {
        envio.accept(
            "Estudante " +
            ev.idEstudante() +
            ": devolução de " +
            ev.idLivro() +
            " em " +
            ev.dataDevolucao() +
            " (vencimento " +
            ev.vencimento() +
            ", atraso " +
            ev.diasAtraso() +
            " dia(s))"
        );
    }
}
