package br.edu.pucpr.ib2b.padroes;

import br.edu.pucpr.ib2b.dominio.*;
import br.edu.pucpr.ib2b.persistencia.*;
import java.util.*;

public final class CadastroPessoa extends CadastroAbstrato<Pessoa> {

    private final RelacionamentoRepositorio repo;

    public CadastroPessoa(FabricaPersistencia f) {
        super(f.unidadeDeTrabalho());
        repo = f.relacionamento();
    }

    protected Optional<Pessoa> buscarExistente(String[] a) {
        return a.length < 1 ? Optional.empty() : repo.pessoa(a[0]);
    }

    protected void validar(String[] a) {
        if (a.length != 3) throw new RegraNegocioException("Informe cpf, nome, sobrenome");
        new Pessoa(a[0], a[1], a[2]);
    }

    protected Pessoa criar(String[] a) {
        return new Pessoa(a[0], a[1], a[2]);
    }

    protected void armazenar(Pessoa p) {
        repo.salvarPessoa(p);
    }
}
