package br.edu.pucpr.ib2b.dominio;

public record Pessoa(String cpf, String nome, String sobrenome) {
    public Pessoa {
        if (cpf == null || !cpf.matches("\\d{11}")) throw new RegraNegocioException(
            "CPF deve conter 11 dígitos"
        );
        if (
            nome == null || nome.isBlank() || sobrenome == null || sobrenome.isBlank()
        ) throw new RegraNegocioException("Nome obrigatório");
    }
}
