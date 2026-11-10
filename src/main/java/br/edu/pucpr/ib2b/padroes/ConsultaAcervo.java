package br.edu.pucpr.ib2b.padroes;

import java.util.*;

public interface ConsultaAcervo {
    Optional<LivroInfo> consultar(String id);

    /** Avisa que os metadados do livro mudaram. Sem cache, não há o que fazer. */
    default void invalidar(String id) {}
}
