package br.edu.pucpr.ib2b.padroes;

import br.edu.pucpr.ib2b.dominio.*;
import br.edu.pucpr.ib2b.persistencia.*;
import java.util.*;

public final class CadastroEstudante extends CadastroAbstrato<Estudante> {

    private final RelacionamentoRepositorio repo;

    public CadastroEstudante(FabricaPersistencia f) {
        super(f.unidadeDeTrabalho());
        repo = f.relacionamento();
    }

    protected Optional<Estudante> buscarExistente(String[] a) {
        return a.length < 1 ? Optional.empty() : repo.estudante(a[0]);
    }

    protected void validar(String[] a) {
        if (a.length != 2) throw new RegraNegocioException("Informe id e CPF");
        new Estudante(a[0], a[1]);
    }

    protected void verificarPreRequisitos(String[] a) {
        if (repo.pessoa(a[1]).isEmpty()) throw new RegraNegocioException(
            "CPF precisa estar cadastrado antes do estudante"
        );
    }

    protected Estudante criar(String[] a) {
        return new Estudante(a[0], a[1]);
    }

    protected void armazenar(Estudante e) {
        repo.salvarEstudante(e);
    }
}
