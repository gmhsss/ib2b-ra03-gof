package br.edu.pucpr.ib2b.padroes;

import java.math.BigDecimal;

/**
 * Adaptee B (simulado): API incompatível com a do banco A.
 * Recebe CPF FORMATADO (000.000.000-00), valor em REAIS (BigDecimal) e uma referência livre.
 * Notifica no formato "status=pago;boleto=B/<ref>" ou "status=cancelado;boleto=B/<ref>".
 */
public final class BancoBSimulado {

    private BigDecimal ultimoValorReais;
    private String ultimoDocumento;
    private int quantidade;

    public String criarBoleto(String cpfFormatado, BigDecimal valorReais, String referencia) {
        if (!cpfFormatado.matches("\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}")) throw new IllegalArgumentException(
            "Banco B: CPF deve estar formatado"
        );
        ultimoValorReais = valorReais;
        ultimoDocumento = cpfFormatado;
        quantidade++;
        return "B/" + referencia;
    }

    /** Simula o retorno que o banco B enviaria após o pagamento. */
    public String retornoDePagamento(String idBoleto) {
        return "status=pago;boleto=" + idBoleto;
    }

    public BigDecimal ultimoValorReais() {
        return ultimoValorReais;
    }

    public String ultimoDocumento() {
        return ultimoDocumento;
    }

    public int quantidade() {
        return quantidade;
    }
}
