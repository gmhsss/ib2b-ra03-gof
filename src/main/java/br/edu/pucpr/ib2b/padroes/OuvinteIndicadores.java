package br.edu.pucpr.ib2b.padroes;

public final class OuvinteIndicadores implements OuvinteDevolucao {

    private int devolucoes;
    private int atrasadas;

    public void aoDevolver(EventoDevolucao e) {
        devolucoes++;
        if (e.diasAtraso() > 0) atrasadas++;
    }

    public int devolucoes() {
        return devolucoes;
    }

    public int atrasadas() {
        return atrasadas;
    }
}
