package br.edu.pucpr.ib2b.padroes;

import java.math.BigDecimal;

/** Adapter A: todas as conversões para/do banco A ficam aqui. */
public final class AdaptadorBancoA implements GatewayCobranca {

    private final BancoASimulado api;

    public AdaptadorBancoA(BancoASimulado api) {
        this.api = api;
    }

    @Override
    public String emitir(String codigo, BigDecimal reais, String cpf) {
        int centavos = reais.movePointRight(2).intValueExact(); // valor: R$ 7,00 -> 700
        String documento = cpf.replaceAll("\\D", ""); // documento: só dígitos
        String nossoNumero = codigo.toUpperCase().replaceAll("[^A-Z0-9]", ""); // identificador: alfanumérico
        return api.gerarCobranca(nossoNumero, centavos, documento);
    }

    @Override
    public NotificacaoPagamento traduzirNotificacao(String aviso) {
        String[] partes = aviso.split("\\|");
        if (partes.length != 2) throw new IllegalArgumentException("Aviso do banco A malformado: " + aviso);
        SituacaoCobranca situacao =
            switch (partes[0]) { // situação: código do banco -> enum institucional
                case "LIQ" -> SituacaoCobranca.PAGA;
                case "CAN" -> SituacaoCobranca.CANCELADA;
                default -> throw new IllegalArgumentException(
                    "Situação desconhecida no banco A: " + partes[0]
                );
            };
        return new NotificacaoPagamento(partes[1], situacao);
    }
}
