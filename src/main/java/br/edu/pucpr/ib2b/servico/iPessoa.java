package br.edu.pucpr.ib2b.servico;

import br.edu.pucpr.ib2b.dominio.*;

public interface iPessoa {
    Pessoa inserirPessoa(String nome, String sobrenome, String cpf);
    Pessoa consultarPessoa(String cpf);
    Pessoa atualizarPessoa(String cpf, String nome, String sobrenome);
    void excluirPessoa(String cpf);
}
