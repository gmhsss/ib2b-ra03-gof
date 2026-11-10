package br.edu.pucpr.ib2b.padroes;

import br.edu.pucpr.ib2b.dominio.*;
import br.edu.pucpr.ib2b.servico.*;
import java.time.*;
import java.util.*;

public interface SolicitacaoCirculacao {
    UUID identificador();
    String autor();
    Instant criadaEm();
    Comprovante executar(CirculacaoFacade fachada);
}
