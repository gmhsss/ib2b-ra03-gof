package br.edu.pucpr.ib2b.dominio;

import java.math.*;
import java.time.*;

public class BoletoMulta {

    private final String codigo, idLivro, idEstudante, politica, codigoExterno;
    private final LocalDate emitidoEm;
    private final BigDecimal valor;
    private boolean quitado;

    public BoletoMulta(
        String codigo,
        String livro,
        String estudante,
        String politica,
        BigDecimal valor,
        LocalDate data,
        String externo,
        boolean quitado
    ) {
        this.codigo = codigo;
        this.idLivro = livro;
        this.idEstudante = estudante;
        this.politica = politica;
        this.valor = valor.setScale(2, RoundingMode.HALF_EVEN);
        this.emitidoEm = data;
        this.codigoExterno = externo;
        this.quitado = quitado;
    }

    public String codigo() {
        return codigo;
    }

    public String idLivro() {
        return idLivro;
    }

    public String idEstudante() {
        return idEstudante;
    }

    public String politica() {
        return politica;
    }

    public BigDecimal valor() {
        return valor;
    }

    public LocalDate emitidoEm() {
        return emitidoEm;
    }

    public String codigoExterno() {
        return codigoExterno;
    }

    public boolean quitado() {
        return quitado;
    }

    public boolean quitar() {
        if (quitado) return false;
        quitado = true;
        return true;
    }

    public BoletoMulta copia() {
        return new BoletoMulta(
            codigo,
            idLivro,
            idEstudante,
            politica,
            valor,
            emitidoEm,
            codigoExterno,
            quitado
        );
    }
}
