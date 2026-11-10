package br.edu.pucpr.ib2b.persistencia;

import br.edu.pucpr.ib2b.dominio.*;
import java.util.*;

public interface MovimentacaoRepositorio {
    Optional<Reserva> reserva(String idLivro);
    void salvarReserva(Reserva r);
    void removerReserva(String idLivro);
    Optional<Emprestimo> emprestimo(String idLivro);
    void salvarEmprestimo(Emprestimo e);
    Optional<Devolucao> devolucao(String idLivro);
    void salvarDevolucao(Devolucao d);
    Optional<BoletoMulta> boleto(String codigo);
    Optional<BoletoMulta> boletoExterno(String codigoExterno);
    void salvarBoleto(BoletoMulta boleto);
    List<BoletoMulta> boletos();
}
