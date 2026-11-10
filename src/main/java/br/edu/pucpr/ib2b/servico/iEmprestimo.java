package br.edu.pucpr.ib2b.servico;

import br.edu.pucpr.ib2b.dominio.*;
import br.edu.pucpr.ib2b.padroes.*;
import java.time.*;
import java.util.*;

public interface iEmprestimo {
    Comprovante processar(SolicitacaoCirculacao comando);
    Comprovante solicitarReserva(String livro, String estudante, LocalDate dia);
    Optional<Reserva> consultarReservas(String livro);
    Comprovante efetivarEmprestimo(String livro, String estudante, LocalDate dia);
    Comprovante renovarEmprestimo(String livro, String estudante, LocalDate dia);
}
