package br.edu.pucpr.ib2b.padroes;

import java.math.BigDecimal;

/** Adapter — Target: contrato institucional de cobrança que o ServicoMultas conhece. */
public interface GatewayCobranca {
    /**
     * Solicita a cobrança no banco.
     * @param codigo identificador institucional da multa
     * @param reais valor em reais (BigDecimal, duas casas)
     * @param cpf CPF do estudante, só dígitos
     * @return identificador da cobrança no banco (usado para casar as notificações)
     */
    String emitir(String codigo, BigDecimal reais, String cpf);

    /** Traduz a notificação no formato do banco para o formato institucional. */
    NotificacaoPagamento traduzirNotificacao(String notificacaoDoBanco);
}
