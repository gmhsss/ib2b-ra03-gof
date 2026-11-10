package br.edu.pucpr.ib2b.padroes;

import br.edu.pucpr.ib2b.dominio.*;
import br.edu.pucpr.ib2b.servico.*;
import java.time.*;
import java.util.*;

public record SolicitacaoRenovacao(
    UUID identificador,
    String autor,
    Instant criadaEm,
    String livro,
    LocalDate dia
)
    implements SolicitacaoCirculacao {
    public SolicitacaoRenovacao {
        Objects.requireNonNull(identificador);
        Objects.requireNonNull(autor);
        Objects.requireNonNull(criadaEm);
    }
    public Comprovante executar(CirculacaoFacade fachada) {
        return fachada.renovar(livro, autor, dia);
    }
}
