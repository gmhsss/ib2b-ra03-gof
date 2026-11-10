package br.edu.pucpr.ib2b.dominio;

import java.time.*;

public class Emprestimo {

    public static final int LIMITE_RENOVACOES = 2;
    public static final int DIAS_POR_PERIODO = 30;

    private final String idLivro, idEstudante;
    private final LocalDate dataInicio;
    private LocalDate vencimento;
    private int numRenovacoes;
    private boolean encerrado;

    public Emprestimo(String livro, String estudante, LocalDate inicio) {
        this(livro, estudante, inicio, inicio.plusDays(DIAS_POR_PERIODO), 0, false);
    }

    public Emprestimo(
        String livro,
        String estudante,
        LocalDate inicio,
        LocalDate vencimento,
        int n,
        boolean encerrado
    ) {
        this.idLivro = livro;
        this.idEstudante = estudante;
        this.dataInicio = inicio;
        this.vencimento = vencimento;
        this.numRenovacoes = n;
        this.encerrado = encerrado;
    }

    public String idLivro() {
        return idLivro;
    }

    public String idEstudante() {
        return idEstudante;
    }

    public LocalDate dataInicio() {
        return dataInicio;
    }

    public LocalDate vencimento() {
        return vencimento;
    }

    public int numRenovacoes() {
        return numRenovacoes;
    }

    public boolean encerrado() {
        return encerrado;
    }

    /** Ação da transição renovar; a guarda fica no estado de origem (Emprestado/Renovado). */
    public void prorrogar() {
        // invariante defensiva: nunca deveria ser violada se o fluxo passar pelo State
        if (encerrado || numRenovacoes >= LIMITE_RENOVACOES) throw new RegraNegocioException(
            "Empréstimo não pode ser prorrogado"
        );
        vencimento = vencimento.plusDays(DIAS_POR_PERIODO);
        numRenovacoes++;
    }

    public void encerrar() {
        if (encerrado) throw new RegraNegocioException("Empréstimo já encerrado");
        encerrado = true;
    }

    public Emprestimo copia() {
        return new Emprestimo(idLivro, idEstudante, dataInicio, vencimento, numRenovacoes, encerrado);
    }
}
