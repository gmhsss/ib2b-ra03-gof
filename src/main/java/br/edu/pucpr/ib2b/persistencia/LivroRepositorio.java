package br.edu.pucpr.ib2b.persistencia;

import br.edu.pucpr.ib2b.dominio.*;
import java.util.*;

public interface LivroRepositorio {
    Optional<Livro> buscar(String id);
    void salvar(Livro livro);
    void remover(String id);
    List<Livro> listar();
}
