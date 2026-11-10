package br.edu.pucpr.ib2b.padroes;

import br.edu.pucpr.ib2b.dominio.*;
import br.edu.pucpr.ib2b.persistencia.*;
import java.util.*;

public final class CadastroAtendente extends CadastroAbstrato<Atendente> {

    private final RelacionamentoRepositorio repo;

    public CadastroAtendente(FabricaPersistencia f) {
        super(f.unidadeDeTrabalho());
        repo = f.relacionamento();
    }

    protected Optional<Atendente> buscarExistente(String[] a) {
        return a.length < 1 ? Optional.empty() : repo.atendente(a[0]);
    }

    protected void validar(String[] a) {
        if (a.length != 2) throw new RegraNegocioException("Informe id e CPF");
        new Atendente(a[0], a[1]);
    }

    protected void verificarPreRequisitos(String[] a) {
        if (repo.pessoa(a[1]).isEmpty()) throw new RegraNegocioException(
            "CPF precisa estar cadastrado antes do atendente"
        );
    }

    protected Atendente criar(String[] a) {
        return new Atendente(a[0], a[1]);
    }

    protected void armazenar(Atendente e) {
        repo.salvarAtendente(e);
    }
}
