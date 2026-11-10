package br.edu.pucpr.ib2b.dominio;

public record Estudante(String id, String cpf) {
    public Estudante {
        if (id == null || id.isBlank() || cpf == null || cpf.isBlank()) throw new RegraNegocioException(
            "Estudante inválido"
        );
    }
}
