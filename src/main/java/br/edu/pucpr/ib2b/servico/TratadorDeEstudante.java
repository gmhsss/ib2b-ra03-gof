package br.edu.pucpr.ib2b.servico;

import br.edu.pucpr.ib2b.dominio.*;
import br.edu.pucpr.ib2b.padroes.*;
import br.edu.pucpr.ib2b.persistencia.*;

public final class TratadorDeEstudante implements iEstudante {

    private final CadastroEstudante cadastro;
    private final Relacionamento rel;

    public TratadorDeEstudante(FabricaPersistencia f) {
        cadastro = new CadastroEstudante(f);
        rel = new Relacionamento(f.relacionamento());
    }

    public Estudante inserirEstudante(String id, String cpf) {
        return cadastro.incluir(id, cpf);
    }

    public Estudante consultarEstudante(String id) {
        return rel.exigirEstudante(id);
    }

    public Estudante atualizarEstudante(String id, String novoCpf) {
        return rel.atualizarEstudante(id, novoCpf);
    }

    public void excluirEstudante(String id) {
        rel.excluirEstudante(id);
    }
}
