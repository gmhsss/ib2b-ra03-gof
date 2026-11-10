package br.edu.pucpr.ib2b.persistencia;

import br.edu.pucpr.ib2b.dominio.*;
import java.util.*;

public interface RelacionamentoRepositorio {
    Optional<Pessoa> pessoa(String cpf);
    Optional<Estudante> estudante(String id);
    Optional<Atendente> atendente(String id);
    void salvarPessoa(Pessoa p);
    void salvarEstudante(Estudante e);
    void salvarAtendente(Atendente a);
    void removerPessoa(String cpf);
    void removerEstudante(String id);
    void removerAtendente(String id);
    boolean cpfTemPapel(String cpf);
}
