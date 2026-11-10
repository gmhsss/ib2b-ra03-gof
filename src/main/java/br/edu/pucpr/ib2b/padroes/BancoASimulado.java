package br.edu.pucpr.ib2b.padroes;

/**
 * Adaptee A (simulado): API incompatível.
 * Recebe valor em CENTAVOS (int), CPF só com dígitos e um "nosso número" alfanumérico.
 * Notifica no formato "LIQ|A-<nossoNumero>" (liquidado) ou "CAN|A-<nossoNumero>" (cancelado).
 */
public final class BancoASimulado {

    private int ultimoValorCentavos;
    private String ultimoDocumento;
    private int quantidade;

    public String gerarCobranca(String nossoNumero, int centavos, String cpfSomenteDigitos) {
        if (!nossoNumero.matches("[A-Z0-9]+")) throw new IllegalArgumentException(
            "Banco A: nosso número inválido"
        );
        if (!cpfSomenteDigitos.matches("\\d{11}")) throw new IllegalArgumentException(
            "Banco A: CPF inválido"
        );
        ultimoValorCentavos = centavos;
        ultimoDocumento = cpfSomenteDigitos;
        quantidade++;
        return "A-" + nossoNumero;
    }

    /** Simula o aviso que o banco A enviaria após o pagamento. */
    public String avisoDeLiquidacao(String idCobranca) {
        return "LIQ|" + idCobranca;
    }

    public int ultimoValorCentavos() {
        return ultimoValorCentavos;
    }

    public String ultimoDocumento() {
        return ultimoDocumento;
    }

    public int quantidade() {
        return quantidade;
    }
}
