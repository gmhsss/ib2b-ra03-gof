package br.edu.pucpr.ib2b.padroes;

import java.math.*;

public final class MultaComTolerancia implements PoliticaMulta {

    public String nome() {
        return "TOLERANCIA_2";
    }

    public BigDecimal calcular(long dias) {
        return BigDecimal.valueOf(Math.max(0, dias - 2)).setScale(2);
    }
}
