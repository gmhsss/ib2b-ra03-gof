package br.edu.pucpr.ib2b.padroes;

import java.math.*;

public interface PoliticaMulta {
    String nome();
    BigDecimal calcular(long diasDeAtraso);
}
