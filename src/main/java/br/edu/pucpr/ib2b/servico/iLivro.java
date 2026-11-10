package br.edu.pucpr.ib2b.servico;

import br.edu.pucpr.ib2b.dominio.*;
import br.edu.pucpr.ib2b.padroes.LivroInfo;
import java.util.Optional;

public interface iLivro {
    Livro inserirLivro(String id, String titulo);
    Livro consultarLivro(String id);
    Livro atualizarLivro(String id, String novoTitulo);
    void excluirLivro(String id);

    /** Metadados (id, título) pela cadeia de decoradores; NÃO serve para decidir reserva. */
    Optional<LivroInfo> consultarMetadados(String id);
}
