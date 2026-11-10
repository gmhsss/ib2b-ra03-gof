package br.edu.pucpr.ib2b.persistencia;

/**
 * Abstract Factory: cria uma FAMÍLIA coerente (UoW + repositórios de Livro, Movimentacao e Relacionamento).
 * FabricaMemoria = família de teste; FabricaMySQL = família de produção. Escolhida uma vez em SistemaIB2B.
 * Como cada fábrica só devolve produtos da própria família, não há como misturar memória com MySQL.
 */
public interface FabricaPersistencia {
    LivroRepositorio livros();
    MovimentacaoRepositorio movimentacao();
    RelacionamentoRepositorio relacionamento();
    UnidadeDeTrabalho unidadeDeTrabalho();
    String familia();
}
