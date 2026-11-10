package br.edu.pucpr.ib2b.padroes;

import java.util.*;

/** Composite — Leaf: referência por ID; não contém Livro, estado nem empréstimo. */
public final class ItemAcervo implements ElementoAcervo {

    private final String livroId;

    public ItemAcervo(String id) {
        this.livroId = Objects.requireNonNull(id);
    }

    public String livroId() {
        return livroId;
    }

    public int contar() {
        return 1;
    }

    public List<String> listarIds() {
        return List.of(livroId);
    }
}
