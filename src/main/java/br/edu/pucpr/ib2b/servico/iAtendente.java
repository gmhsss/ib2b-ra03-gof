package br.edu.pucpr.ib2b.servico;

import br.edu.pucpr.ib2b.dominio.*;

public interface iAtendente {
    Atendente inserirAtendente(String id, String cpf);
    Atendente consultarAtendente(String id);
    Atendente atualizarAtendente(String id, String novoCpf);
    void excluirAtendente(String id);
}
