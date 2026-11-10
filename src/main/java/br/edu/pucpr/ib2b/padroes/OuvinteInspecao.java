package br.edu.pucpr.ib2b.padroes;

import java.util.*;

public final class OuvinteInspecao implements OuvinteDevolucao {

    private final List<String> fila = new ArrayList<>();

    public void aoDevolver(EventoDevolucao e) {
        fila.add(e.idLivro());
    }

    public List<String> fila() {
        return List.copyOf(fila);
    }
}
