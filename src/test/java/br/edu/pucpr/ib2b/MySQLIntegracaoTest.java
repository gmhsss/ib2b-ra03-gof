package br.edu.pucpr.ib2b;

import static org.junit.jupiter.api.Assertions.*;

import br.edu.pucpr.ib2b.dominio.*;
import br.edu.pucpr.ib2b.persistencia.*;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

/** Opcional: executar com IB2B_TESTAR_MYSQL=true e servidor MySQL disponível. */
public class MySQLIntegracaoTest {

    @Test
    @EnabledIfEnvironmentVariable(named = "IB2B_TESTAR_MYSQL", matches = "true")
    void R08_mysql_repositorios_compartilham_transacao() {
        var f = new FabricaMySQL(
            System.getenv().getOrDefault("IB2B_DB_URL", "jdbc:mysql://localhost:3306/ib2b?allowPublicKeyRetrieval=true"),
            System.getenv().getOrDefault("IB2B_DB_USER", "ib2b"),
            System.getenv().getOrDefault("IB2B_DB_PASSWORD", "ib2b")
        );
        var id = "R08-" + UUID.randomUUID();
        assertThrows(RegraNegocioException.class, () ->
            f
                .unidadeDeTrabalho()
                .executar(() -> {
                    f.livros().salvar(new Livro(id, "Teste transacional"));
                    throw new RegraNegocioException("Provocar rollback");
                })
        );
        assertTrue(f.livros().buscar(id).isEmpty(), "A gravação deve ser desfeita no mesmo banco");
        f
            .unidadeDeTrabalho()
            .executar(() -> {
                f.livros().salvar(new Livro(id, "Teste commit"));
                f.relacionamento().salvarPessoa(new Pessoa("12345678901", "Ana", "Silva"));
                return null;
            });
        assertTrue(f.livros().buscar(id).isPresent(), "A transação deve confirmar os dados");
    }
}
