package br.edu.pucpr.ib2b.servico;

import br.edu.pucpr.ib2b.dominio.*;
import java.time.*;
import java.util.*;

public interface iDevolucao {
    Comprovante registrarDevolucao(String livro, String estudante, LocalDate dia);

    Optional<BoletoMulta> emitirBoletoMulta(String livro, String estudante);

    Optional<BoletoMulta> consultarMulta(String livro, String estudante);

    boolean registrarPagamento(String notificacaoDoBanco);

    Comprovante inspecionar(String livro, boolean apto, LocalDate dia);

    Comprovante concluirReparo(String livro, LocalDate dia);

    Comprovante laudoIrrecuperavel(String livro, LocalDate dia);
}
