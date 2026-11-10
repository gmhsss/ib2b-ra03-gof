package br.edu.pucpr.ib2b.padroes;

import br.edu.pucpr.ib2b.dominio.*;
import br.edu.pucpr.ib2b.persistencia.*;
import java.util.*;

public final class CadastroLivro extends CadastroAbstrato<Livro> {

    private final LivroRepositorio repo;

    public CadastroLivro(FabricaPersistencia f) {
        super(f.unidadeDeTrabalho());
        repo = f.livros();
    }

    protected Optional<Livro> buscarExistente(String[] a) {
        return a.length < 1 ? Optional.empty() : repo.buscar(a[0]);
    }

    protected void validar(String[] a) {
        if (a.length != 2) throw new RegraNegocioException("Informe id e título");
        new Livro(a[0], a[1]);
    }

    protected Livro criar(String[] a) {
        return new Livro(a[0], a[1]);
    }

    protected void armazenar(Livro l) {
        repo.salvar(l);
    }
}
