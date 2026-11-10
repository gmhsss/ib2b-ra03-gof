package br.edu.pucpr.ib2b.dominio;

import java.time.*;

public record Reserva(String idLivro, String idEstudante, LocalDate criadaEm) {}
