package br.edu.pucpr.ib2b.padroes;

import br.edu.pucpr.ib2b.persistencia.*;
import java.util.*;

/** Template Method: ordem fixa, final; etapas variáveis nas subclasses. */
public abstract class CadastroAbstrato<T> {

    private final UnidadeDeTrabalho unidade;

    protected CadastroAbstrato(UnidadeDeTrabalho unidade) {
        this.unidade = unidade;
    }

    public final T incluir(String... entrada) {
        return unidade.executar(() -> {
            var jaExiste = buscarExistente(entrada);
            if (jaExiste.isPresent()) return apresentar(jaExiste.get());
            validar(entrada);
            verificarPreRequisitos(entrada);
            T objeto = criar(entrada);
            armazenar(objeto);
            return apresentar(objeto);
        });
    }

    protected abstract Optional<T> buscarExistente(String[] entrada);

    protected abstract void validar(String[] entrada);

    protected void verificarPreRequisitos(String[] entrada) {} // hook opcional

    protected abstract T criar(String[] entrada);

    protected abstract void armazenar(T objeto);

    protected T apresentar(T objeto) {
        return objeto;
    } // hook opcional
}
