package br.edu.pucpr.ib2b.padroes;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Observer — Subject: conhece apenas a interface OuvinteDevolucao.
 * Um ouvinte que falha vai para a fila de reenvio; os demais continuam sendo notificados
 * e a devolução (já confirmada antes da publicação) não é desfeita.
 */
public final class PublicadorDevolucao {

    private final List<OuvinteDevolucao> ouvintes = new CopyOnWriteArrayList<>();
    private final List<FalhaNotificacao> filaDeReenvio = new CopyOnWriteArrayList<>();

    public void registrar(OuvinteDevolucao ouvinte) {
        ouvintes.add(Objects.requireNonNull(ouvinte));
    }

    public void remover(OuvinteDevolucao ouvinte) {
        ouvintes.remove(ouvinte);
    }

    public void publicar(EventoDevolucao evento) {
        for (var ouvinte : ouvintes) notificar(ouvinte, evento);
    }

    /** Tenta de novo os ouvintes que falharam; os que ainda falharem continuam na fila. */
    public int reenviarFalhas() {
        var pendentes = List.copyOf(filaDeReenvio);
        filaDeReenvio.clear();
        int reenviados = 0;
        for (var f : pendentes) if (notificar(f.ouvinte(), f.evento())) reenviados++;
        return reenviados;
    }

    public List<FalhaNotificacao> falhas() {
        return List.copyOf(filaDeReenvio);
    }

    private boolean notificar(OuvinteDevolucao ouvinte, EventoDevolucao evento) {
        try {
            ouvinte.aoDevolver(evento);
            return true;
        } catch (RuntimeException erro) {
            filaDeReenvio.add(new FalhaNotificacao(ouvinte, evento, erro));
            return false;
        }
    }
}
