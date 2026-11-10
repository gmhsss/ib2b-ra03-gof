package br.edu.pucpr.ib2b.dominio;

import java.time.*;

public record Comprovante(String operacao, String livro, String estudante, LocalDate data, String detalhe) {}
