package br.edu.pucpr.ib2b.padroes;

/** Notificação já traduzida para o vocabulário institucional (saída do Adapter). */
public record NotificacaoPagamento(String codigoExterno, SituacaoCobranca situacao) {}
