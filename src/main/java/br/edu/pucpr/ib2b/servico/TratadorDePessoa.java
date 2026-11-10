package br.edu.pucpr.ib2b.servico;

import br.edu.pucpr.ib2b.dominio.*;
import br.edu.pucpr.ib2b.padroes.*;
import br.edu.pucpr.ib2b.persistencia.*;

public final class TratadorDePessoa implements iPessoa {

    private final CadastroPessoa cadastro;
    private final Relacionamento relacionamento;

    public TratadorDePessoa(FabricaPersistencia f) {
        cadastro = new CadastroPessoa(f);
        relacionamento = new Relacionamento(f.relacionamento());
    }

    public Pessoa inserirPessoa(String nome, String sobrenome, String cpf) {
        return cadastro.incluir(cpf, nome, sobrenome);
    }

    public Pessoa consultarPessoa(String cpf) {
        return relacionamento.exigirPessoa(cpf);
    }

    public Pessoa atualizarPessoa(String cpf, String nome, String sobrenome) {
        return relacionamento.atualizarPessoa(cpf, nome, sobrenome);
    }

    public void excluirPessoa(String cpf) {
        relacionamento.excluirPessoa(cpf);
    }
}
