package br.edu.pucpr.ib2b.estado;

import br.edu.pucpr.ib2b.dominio.*;

/**
 * State: cada evento da máquina de estados de Livro é um método.
 * Por padrão o evento é recusado (exceção) SEM alterar o livro;
 * cada estado concreto sobrescreve apenas as transições que aceita.
 */
public interface EstadoDoLivro {
    EstadoLivro tipo();

    default void reservar(Livro l) {
        throw invalido("reservar");
    }

    default void emprestar(Livro l) {
        throw invalido("emprestar");
    }

    default void renovar(Livro l, Emprestimo e) {
        throw invalido("renovar");
    }

    default void devolver(Livro l) {
        throw invalido("devolver");
    }

    default void inspecionar(Livro l, boolean apto) {
        throw invalido("inspecionar");
    }

    default void encaminharParaReparo(Livro l) {
        throw invalido("encaminhar para reparo");
    }

    default void concluirReparo(Livro l) {
        throw invalido("concluir reparo");
    }

    default void laudoIrrecuperavel(Livro l) {
        throw invalido("descartar");
    }

    /** Não é transição: só confirma que o cadastro do livro pode ser removido neste estado. */
    default void verificarExclusao(Livro l) {
        throw invalido("excluir cadastro");
    }

    default RegraNegocioException invalido(String acao) {
        return new RegraNegocioException("Ação '" + acao + "' proibida no estado " + tipo());
    }
}
