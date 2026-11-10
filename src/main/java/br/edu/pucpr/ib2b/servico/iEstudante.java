package br.edu.pucpr.ib2b.servico;

import br.edu.pucpr.ib2b.dominio.*;

public interface iEstudante {
    Estudante inserirEstudante(String id, String cpf);
    Estudante consultarEstudante(String id);
    Estudante atualizarEstudante(String id, String novoCpf);
    void excluirEstudante(String id);
}
