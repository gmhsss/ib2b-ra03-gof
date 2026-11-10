package br.edu.pucpr.ib2b.app;

import br.edu.pucpr.ib2b.padroes.*;
import br.edu.pucpr.ib2b.persistencia.*;
import br.edu.pucpr.ib2b.servico.*;
import java.util.*;

/**
 * Composição (montagem) do sistema: é o ÚNICO lugar que conhece classes concretas.
 * Aqui são escolhidos, uma vez, a família de persistência, o banco, a política de multa
 * e a cadeia de decoradores da consulta.
 */
public final class SistemaIB2B {

    public final FabricaPersistencia fabrica;
    public final iPessoa pessoas;
    public final iEstudante estudantes;
    public final iAtendente atendentes;
    public final iLivro livros;
    public final iEmprestimo emprestimos;
    public final iDevolucao devolucoes;
    // objetos internos expostos apenas para os testes e para a arguição
    public final ServicoMultas multas;
    public final CirculacaoFacade fachada;
    public final PublicadorDevolucao publicador;
    public final OuvinteInspecao inspecao;
    public final OuvinteIndicadores indicadores;

    public SistemaIB2B(FabricaPersistencia fabrica, GatewayCobranca gateway, PoliticaMulta politica) {
        this(fabrica, gateway, politica, "medicao,cache");
    }

    public SistemaIB2B(
        FabricaPersistencia fabrica,
        GatewayCobranca gateway,
        PoliticaMulta politica,
        String decoradoresConsulta
    ) {
        this.fabrica = fabrica;
        this.multas = new ServicoMultas(fabrica, gateway, politica);
        // Observer: novas reações = novos ouvintes registrados aqui, sem alterar tratador nem fachada
        this.publicador = new PublicadorDevolucao();
        this.inspecao = new OuvinteInspecao();
        this.indicadores = new OuvinteIndicadores();
        publicador.registrar(inspecao);
        publicador.registrar(new OuvinteMultas(multas));
        publicador.registrar(indicadores);
        publicador.registrar(new OuvinteEmail(mensagem -> {}));
        this.fachada = new CirculacaoFacade(fabrica, publicador);
        this.pessoas = new TratadorDePessoa(fabrica);
        this.estudantes = new TratadorDeEstudante(fabrica);
        this.atendentes = new TratadorDeAtendente(fabrica);
        this.livros = new TratadorDeAcervo(fabrica, montarConsulta(fabrica.livros(), decoradoresConsulta));
        this.emprestimos = new TratadorDeEmprestimo(fachada);
        this.devolucoes = new TratadorDeDevolucao(fachada, multas);
    }

    /**
     * Decorator: monta a cadeia a partir da configuração, de FORA para DENTRO.
     * "medicao,cache" => Medicao(Cache(Original)): a medição vê também os acertos do cache.
     * "cache,medicao" => Cache(Medicao(Original)): acertos do cache NÃO seriam medidos.
     */
    public static ConsultaAcervo montarConsulta(LivroRepositorio repo, String configuracao) {
        ConsultaAcervo consulta = new ConsultaOriginal(repo);
        List<String> camadas = new ArrayList<>(
            Arrays.stream(configuracao.split(",")).map(String::trim).filter(c -> !c.isEmpty()).toList()
        );
        Collections.reverse(camadas); // embrulha de dentro para fora
        for (String camada : camadas) {
            consulta = switch (camada.toLowerCase()) {
                case "cache" -> new ConsultaComCache(consulta);
                case "medicao" -> new ConsultaComMedicao(consulta);
                default -> throw new IllegalArgumentException("Decorador desconhecido: " + camada);
            };
        }
        return consulta;
    }

    public static SistemaIB2B inicializar(String perfil, String banco, String politica) {
        return inicializar(perfil, banco, politica, "medicao,cache");
    }

    public static SistemaIB2B inicializar(String perfil, String banco, String politica, String consulta) {
        // Abstract Factory: a família é escolhida UMA vez, pelo perfil de execução
        FabricaPersistencia fabrica =
            switch (perfil.toLowerCase()) {
                case "memoria" -> new FabricaMemoria();
                case "mysql" -> new FabricaMySQL(
                    System.getenv().getOrDefault("IB2B_DB_URL", "jdbc:mysql://localhost:3306/ib2b?allowPublicKeyRetrieval=true"),
                    System.getenv().getOrDefault("IB2B_DB_USER", "ib2b"),
                    System.getenv().getOrDefault("IB2B_DB_PASSWORD", "ib2b")
                );
                default -> throw new IllegalArgumentException("Perfil inválido: " + perfil);
            };
        // Adapter: o banco da instalação é escolhido aqui; ServicoMultas não sabe qual é
        GatewayCobranca g =
            switch (banco.toUpperCase()) {
                case "A" -> new AdaptadorBancoA(new BancoASimulado());
                case "B" -> new AdaptadorBancoB(new BancoBSimulado());
                default -> throw new IllegalArgumentException("Banco inválido: " + banco);
            };
        // Strategy: política vigente definida pela configuração administrativa
        PoliticaMulta p =
            switch (politica.toLowerCase()) {
                case "original" -> new MultaOriginal();
                case "teto" -> new MultaComTeto();
                case "tolerancia" -> new MultaComTolerancia();
                default -> throw new IllegalArgumentException("Política inválida: " + politica);
            };
        return new SistemaIB2B(fabrica, g, p, consulta);
    }
}
