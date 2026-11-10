package br.edu.pucpr.ib2b.padroes;

import java.math.*;

public final class MultaOriginal implements PoliticaMulta {

    public String nome() {
        return "ORIGINAL";
    }

    public BigDecimal calcular(long dias) {
        return BigDecimal.valueOf(Math.max(0, dias)).setScale(2);
    }
}
