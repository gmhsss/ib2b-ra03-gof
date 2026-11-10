package br.edu.pucpr.ib2b.dominio;

public record Atendente(String id, String cpf) {
    public Atendente {
        if (id == null || id.isBlank() || cpf == null || cpf.isBlank()) throw new RegraNegocioException(
            "Atendente inválido"
        );
    }
}
