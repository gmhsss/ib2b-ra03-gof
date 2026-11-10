package br.edu.pucpr.ib2b.padroes;

import java.math.*;

public final class MultaComTeto implements PoliticaMulta {

    public String nome() {
        return "TETO_20";
    }

    public BigDecimal calcular(long dias) {
        return BigDecimal.valueOf(Math.min(20, Math.max(0, dias))).setScale(2);
    }
}
