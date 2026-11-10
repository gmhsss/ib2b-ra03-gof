package br.edu.pucpr.ib2b.servico;

import br.edu.pucpr.ib2b.dominio.*;
import br.edu.pucpr.ib2b.persistencia.*;

public final class Relacionamento {

    private final RelacionamentoRepositorio repo;

    public Relacionamento(RelacionamentoRepositorio repo) {
        this.repo = repo;
    }

    public Pessoa exigirPessoa(String cpf) {
        return repo.pessoa(cpf).orElseThrow(() -> new RegraNegocioException("Pessoa não cadastrada"));
    }

    public Estudante exigirEstudante(String id) {
        return repo
            .estudante(id)
            .orElseThrow(() -> new RegraNegocioException("Estudante não cadastrado: " + id));
    }

    public Atendente exigirAtendente(String id) {
        return repo
            .atendente(id)
            .orElseThrow(() -> new RegraNegocioException("Atendente não cadastrado: " + id));
    }

    public Pessoa atualizarPessoa(String cpf, String nome, String sobrenome) {
        exigirPessoa(cpf);
        var p = new Pessoa(cpf, nome, sobrenome);
        repo.salvarPessoa(p);
        return p;
    }

    public Estudante atualizarEstudante(String id, String cpf) {
        exigirEstudante(id);
        exigirPessoa(cpf);
        var e = new Estudante(id, cpf);
        repo.salvarEstudante(e);
        return e;
    }

    public Atendente atualizarAtendente(String id, String cpf) {
        exigirAtendente(id);
        exigirPessoa(cpf);
        var a = new Atendente(id, cpf);
        repo.salvarAtendente(a);
        return a;
    }

    public void excluirPessoa(String cpf) {
        exigirPessoa(cpf);
        if (repo.cpfTemPapel(cpf)) throw new RegraNegocioException(
            "Pessoa vinculada a papéis; exclusão proibida"
        );
        repo.removerPessoa(cpf);
    }

    public void excluirEstudante(String id) {
        exigirEstudante(id);
        repo.removerEstudante(id);
    }

    public void excluirAtendente(String id) {
        exigirAtendente(id);
        repo.removerAtendente(id);
    }
}
