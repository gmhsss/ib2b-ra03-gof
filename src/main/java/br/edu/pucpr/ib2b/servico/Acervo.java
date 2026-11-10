package br.edu.pucpr.ib2b.servico;

import br.edu.pucpr.ib2b.dominio.*;
import br.edu.pucpr.ib2b.persistencia.*;

public final class Acervo {

    private final LivroRepositorio repo;

    public Acervo(LivroRepositorio repo) {
        this.repo = repo;
    }

    public Livro exigir(String id) {
        return repo.buscar(id).orElseThrow(() -> new RegraNegocioException("Livro não encontrado: " + id));
    }

    public void atualizar(Livro l) {
        repo.salvar(l);
    }

    public Livro atualizarTitulo(String id, String novoTitulo) {
        Livro anterior = exigir(id), novo = new Livro(id, novoTitulo, anterior.estado());
        repo.salvar(novo);
        return novo;
    }

    public void excluir(String id) {
        Livro livro = exigir(id);
        livro.verificarExclusao(); // o estado decide; Acervo não testa a situação do livro
        repo.remover(id);
    }
}
