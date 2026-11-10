package br.edu.pucpr.ib2b.dominio;

public class RegraNegocioException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public RegraNegocioException(String motivo) {
        super(motivo);
    }
}
