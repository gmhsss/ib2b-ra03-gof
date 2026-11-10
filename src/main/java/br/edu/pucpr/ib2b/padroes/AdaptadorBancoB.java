package br.edu.pucpr.ib2b.padroes;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

/** Adapter B: todas as conversões para/do banco B ficam aqui. */
public final class AdaptadorBancoB implements GatewayCobranca {

    private final BancoBSimulado api;

    public AdaptadorBancoB(BancoBSimulado api) {
        this.api = api;
    }

    @Override
    public String emitir(String codigo, BigDecimal reais, String cpf) {
        BigDecimal valor = reais.setScale(2, RoundingMode.HALF_EVEN); // valor: R$ 7,00 -> 7.00
        String d = cpf.replaceAll("\\D", "");
        String documento =
            d.substring(0, 3) + "." + d.substring(3, 6) + "." + d.substring(6, 9) + "-" + d.substring(9);
        return api.criarBoleto(documento, valor, codigo); // identificador: referência livre
    }

    @Override
    public NotificacaoPagamento traduzirNotificacao(String retorno) {
        Map<String, String> campos = new HashMap<>();
        for (String par : retorno.split(";")) {
            String[] kv = par.split("=", 2);
            if (kv.length == 2) campos.put(kv[0].trim(), kv[1].trim());
        }
        String boleto = campos.get("boleto");
        if (boleto == null) throw new IllegalArgumentException("Retorno do banco B sem boleto: " + retorno);
        SituacaoCobranca situacao =
            switch (campos.getOrDefault("status", "")) {
                case "pago" -> SituacaoCobranca.PAGA;
                case "cancelado" -> SituacaoCobranca.CANCELADA;
                default -> throw new IllegalArgumentException("Situação desconhecida no banco B: " + retorno);
            };
        return new NotificacaoPagamento(boleto, situacao);
    }
}
