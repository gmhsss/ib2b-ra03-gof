package br.edu.pucpr.ib2b.padroes;

/** Registro de um ouvinte que falhou: fica na fila de reenvio do publicador. */
public record FalhaNotificacao(OuvinteDevolucao ouvinte, EventoDevolucao evento, RuntimeException erro) {}
