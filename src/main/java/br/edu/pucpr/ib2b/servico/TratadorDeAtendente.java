package br.edu.pucpr.ib2b.servico;

import br.edu.pucpr.ib2b.dominio.*;
import br.edu.pucpr.ib2b.padroes.*;
import br.edu.pucpr.ib2b.persistencia.*;

public final class TratadorDeAtendente implements iAtendente {

    private final CadastroAtendente cadastro;
    private final Relacionamento rel;

    public TratadorDeAtendente(FabricaPersistencia f) {
        cadastro = new CadastroAtendente(f);
        rel = new Relacionamento(f.relacionamento());
    }

    public Atendente inserirAtendente(String id, String cpf) {
        return cadastro.incluir(id, cpf);
    }

    public Atendente consultarAtendente(String id) {
        return rel.exigirAtendente(id);
    }

    public Atendente atualizarAtendente(String id, String novoCpf) {
        return rel.atualizarAtendente(id, novoCpf);
    }

    public void excluirAtendente(String id) {
        rel.excluirAtendente(id);
    }
}
