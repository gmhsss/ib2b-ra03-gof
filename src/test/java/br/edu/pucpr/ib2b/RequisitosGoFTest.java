package br.edu.pucpr.ib2b;

import static org.junit.jupiter.api.Assertions.*;

import br.edu.pucpr.ib2b.app.SistemaIB2B;
import br.edu.pucpr.ib2b.dominio.*;
import br.edu.pucpr.ib2b.padroes.*;
import br.edu.pucpr.ib2b.persistencia.*;
import br.edu.pucpr.ib2b.servico.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.Test;

/** Cenários de verificação do enunciado: o prefixo Rnn identifica o requisito (padrão) testado. */
class RequisitosGoFTest {

    private static final LocalDate D = LocalDate.of(2026, 11, 9);
    private static final String CPF = "12345678901";

    /** Sistema em memória com Ana (CPF), estudante E01 e livro L01 LIVRE. */
    private static SistemaIB2B sistemaComLivro() {
        var app = new SistemaIB2B(
            new FabricaMemoria(),
            new AdaptadorBancoA(new BancoASimulado()),
            new MultaOriginal()
        );
        app.pessoas.inserirPessoa("Ana", "Silva", CPF);
        app.estudantes.inserirEstudante("E01", CPF);
        app.livros.inserirLivro("L01", "UML aplicado");
        return app;
    }

    private static SistemaIB2B sistemaComEmprestimo() {
        var app = sistemaComLivro();
        app.fachada.reservar("L01", "E01", D);
        app.fachada.efetivarEmprestimo("L01", "E01", D); // vence em D+30
        return app;
    }

    private static Emprestimo emprestimo(SistemaIB2B app) {
        return app.fabrica.movimentacao().emprestimo("L01").orElseThrow();
    }

    private static EstadoLivro estado(SistemaIB2B app) {
        return app.livros.consultarLivro("L01").estado();
    }

    // ===================== R01 — State =====================

    @Test
    void R01_terceiraRenovacaoRecusada() {
        var app = sistemaComEmprestimo();
        app.fachada.renovar("L01", "E01", D.plusDays(1));
        app.fachada.renovar("L01", "E01", D.plusDays(2));
        LocalDate vencimentoAntes = emprestimo(app).vencimento();

        assertThrows(RegraNegocioException.class, () -> app.fachada.renovar("L01", "E01", D.plusDays(3)));

        assertEquals(vencimentoAntes, emprestimo(app).vencimento(), "vencimento não pode mudar");
        assertEquals(D.plusDays(90), emprestimo(app).vencimento());
        assertEquals(2, emprestimo(app).numRenovacoes());
        assertEquals(EstadoLivro.RENOVADO, estado(app));
    }

    @Test
    void R01_devolvidoNaoInspecionadoNaoPodeSerReservado() {
        var app = sistemaComEmprestimo();
        app.fachada.devolver("L01", "E01", D.plusDays(10));
        assertEquals(EstadoLivro.DEVOLVIDO, estado(app));

        assertThrows(RegraNegocioException.class, () -> app.fachada.reservar("L01", "E01", D.plusDays(10)));
        assertEquals(EstadoLivro.DEVOLVIDO, estado(app), "evento recusado não altera o livro");
        assertTrue(app.emprestimos.consultarReservas("L01").isEmpty(), "rollback: nenhuma reserva gravada");
    }

    @Test
    void R01_laudoIrrecuperavelLevaReparandoAIncinerado() {
        var app = sistemaComEmprestimo();
        app.fachada.devolver("L01", "E01", D.plusDays(10));
        app.devolucoes.inspecionar("L01", false, D.plusDays(10));
        assertEquals(EstadoLivro.REPARANDO, estado(app));
        app.devolucoes.laudoIrrecuperavel("L01", D.plusDays(11));
        assertEquals(EstadoLivro.INCINERADO, estado(app));
        assertThrows(RegraNegocioException.class, () -> app.fachada.reservar("L01", "E01", D.plusDays(12)));
    }

    @Test
    void R01_estadoGravadoEReconstituido() {
        var livro = new Livro("L09", "Algoritmos", EstadoLivro.REPARANDO); // como vem do banco
        assertThrows(RegraNegocioException.class, livro::reservar);
        assertEquals(EstadoLivro.REPARANDO, livro.estado());
        livro.concluirReparo();
        assertEquals(EstadoLivro.LIVRE, livro.estado());
    }

    // ===================== R02 — Strategy =====================

    @Test
    void R02_cincoDiasDeAtrasoNasPoliticas() {
        assertEquals(new BigDecimal("5.00"), new MultaOriginal().calcular(5));
        assertEquals(new BigDecimal("3.00"), new MultaComTolerancia().calcular(5));
        assertEquals(new BigDecimal("5.00"), new MultaComTeto().calcular(5));
        assertEquals(new BigDecimal("20.00"), new MultaComTeto().calcular(45));
        for (PoliticaMulta p : List.of(new MultaOriginal(), new MultaComTolerancia(), new MultaComTeto())) {
            assertEquals(0, p.calcular(0).signum(), "sem atraso => zero em " + p.nome());
        }
    }

    @Test
    void R02_trocaDePoliticaNaoRecalculaMultaEmitida() {
        var app = sistemaComEmprestimo();
        app.fachada.devolver("L01", "E01", D.plusDays(35)); // 5 dias de atraso
        var emitida = app.devolucoes.consultarMulta("L01", "E01").orElseThrow();
        assertEquals(new BigDecimal("5.00"), emitida.valor());
        assertEquals("ORIGINAL", emitida.politica());

        app.multas.configurarPolitica(new MultaComTolerancia());

        var relida = app.devolucoes.consultarMulta("L01", "E01").orElseThrow();
        assertEquals(new BigDecimal("5.00"), relida.valor(), "valor gravado na emissão");
        assertEquals("ORIGINAL", relida.politica(), "política gravada na emissão");
        assertEquals(relida.codigo(), app.devolucoes.emitirBoletoMulta("L01", "E01").orElseThrow().codigo());
    }

    // ===================== R03 — Adapter =====================

    private static ServicoMultas servicoCom(GatewayCobranca banco) {
        var f = new FabricaMemoria();
        new CadastroPessoa(f).incluir(CPF, "Ana", "Silva");
        new CadastroEstudante(f).incluir("E01", CPF);
        return new ServicoMultas(f, banco, new MultaOriginal());
    }

    @Test
    void R03_mesmaSolicitacaoGera700CentavosNoAe7ReaisNoB() {
        var bancoA = new BancoASimulado();
        var bancoB = new BancoBSimulado();
        var devolucao = new Devolucao("L01", "E01", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 8)); // 7 dias

        servicoCom(new AdaptadorBancoA(bancoA)).emitir(devolucao).orElseThrow();
        servicoCom(new AdaptadorBancoB(bancoB)).emitir(devolucao).orElseThrow();

        assertEquals(700, bancoA.ultimoValorCentavos());
        assertEquals(new BigDecimal("7.00"), bancoB.ultimoValorReais());
        assertEquals("12345678901", bancoA.ultimoDocumento(), "A recebe CPF só com dígitos");
        assertEquals("123.456.789-01", bancoB.ultimoDocumento(), "B recebe CPF formatado");
    }

    @Test
    void R03_notificacaoRepetidaNaoGeraSegundaQuitacao() {
        var bancoA = new BancoASimulado();
        var servicoA = servicoCom(new AdaptadorBancoA(bancoA));
        var devolucao = new Devolucao("L01", "E01", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 8));
        var boletoA = servicoA.emitir(devolucao).orElseThrow();
        String avisoA = bancoA.avisoDeLiquidacao(boletoA.codigoExterno());
        assertTrue(servicoA.registrarNotificacao(avisoA));
        assertFalse(servicoA.registrarNotificacao(avisoA), "aviso repetido não quita de novo");
        assertTrue(servicoA.multaDa(devolucao).orElseThrow().quitado());

        var bancoB = new BancoBSimulado();
        var servicoB = servicoCom(new AdaptadorBancoB(bancoB));
        var boletoB = servicoB.emitir(devolucao).orElseThrow();
        String retornoB = bancoB.retornoDePagamento(boletoB.codigoExterno());
        assertTrue(servicoB.registrarNotificacao(retornoB));
        assertFalse(servicoB.registrarNotificacao(retornoB));
    }

    // ===================== R04 — Facade =====================

    @Test
    void R04_efetivacaoValidaReservaRemoveReservaCriaEmprestimoEAtualizaLivro() {
        var app = sistemaComLivro();
        app.emprestimos.solicitarReserva("L01", "E01", D);
        assertTrue(app.emprestimos.consultarReservas("L01").isPresent());

        Comprovante c = app.emprestimos.efetivarEmprestimo("L01", "E01", D);

        assertEquals("EMPRESTIMO", c.operacao(), "devolve comprovante, não coleção");
        assertTrue(app.emprestimos.consultarReservas("L01").isEmpty(), "reserva ativa removida");
        assertEquals(D.plusDays(30), emprestimo(app).vencimento(), "empréstimo criado");
        assertEquals(EstadoLivro.EMPRESTADO, estado(app), "Livro atualizado");
    }

    @Test
    void R04_falhaNoMeioDaEfetivacaoDesfazTudo() {
        var app = sistemaComLivro();
        app.fachada.reservar("L01", "E01", D);
        var fachadaComFalha = new CirculacaoFacade(app.fabrica, new PublicadorDevolucao(), () -> {
            throw new RegraNegocioException("falha injetada após remover a reserva");
        });

        assertThrows(RegraNegocioException.class, () -> fachadaComFalha.efetivarEmprestimo("L01", "E01", D));

        assertTrue(app.emprestimos.consultarReservas("L01").isPresent(), "reserva restaurada");
        assertTrue(app.fabrica.movimentacao().emprestimo("L01").isEmpty(), "empréstimo não criado");
        assertEquals(EstadoLivro.RESERVADO, estado(app), "estado restaurado");
    }

    @Test
    void R04_reservaDeOutroEstudanteNaoPodeSerEfetivada() {
        var app = sistemaComLivro();
        app.pessoas.inserirPessoa("Bia", "Souza", "10987654321");
        app.estudantes.inserirEstudante("E02", "10987654321");
        app.fachada.reservar("L01", "E01", D);
        assertThrows(RegraNegocioException.class, () -> app.emprestimos.efetivarEmprestimo("L01", "E02", D));
        assertEquals(EstadoLivro.RESERVADO, estado(app));
    }

    // ===================== R05 — Observer =====================

    @Test
    void R05_devolucaoAtrasadaAlimentaInspecaoMultaEIndicadores() {
        var app = sistemaComEmprestimo();
        app.devolucoes.registrarDevolucao("L01", "E01", D.plusDays(35));

        assertEquals(List.of("L01"), app.inspecao.fila());
        assertTrue(app.devolucoes.consultarMulta("L01", "E01").isPresent());
        assertEquals(1, app.indicadores.devolucoes());
        assertEquals(1, app.indicadores.atrasadas());
        assertEquals(EstadoLivro.DEVOLVIDO, estado(app), "permanece DEVOLVIDO até a inspeção");
    }

    @Test
    void R05_devolucaoNoPrazoNaoGeraMulta() {
        var app = sistemaComEmprestimo();
        app.devolucoes.registrarDevolucao("L01", "E01", D.plusDays(30));
        assertTrue(app.devolucoes.consultarMulta("L01", "E01").isEmpty());
        assertEquals(0, app.indicadores.atrasadas());
        assertEquals(List.of("L01"), app.inspecao.fila());
    }

    @Test
    void R05_falhaDoEmailNaoAfetaOutrosOuvintesNemDesfazDevolucao() {
        var app = sistemaComEmprestimo();
        var smtpNoAr = new boolean[] { false };
        var enviados = new ArrayList<String>();
        app.publicador.registrar(
            new OuvinteEmail(msg -> {
                if (!smtpNoAr[0]) throw new IllegalStateException("SMTP indisponível");
                enviados.add(msg);
            })
        );
        var depois = new int[1];
        app.publicador.registrar(ev -> depois[0]++);

        app.devolucoes.registrarDevolucao("L01", "E01", D.plusDays(35));

        assertEquals(EstadoLivro.DEVOLVIDO, estado(app), "devolução não desfeita");
        assertEquals(1, depois[0], "ouvinte registrado depois do e-mail também foi chamado");
        assertEquals(1, app.publicador.falhas().size(), "falha foi para a fila de reenvio");

        smtpNoAr[0] = true;
        assertEquals(1, app.publicador.reenviarFalhas());
        assertTrue(app.publicador.falhas().isEmpty());
        assertEquals(1, enviados.size());
    }

    // ===================== R06 — Command =====================

    @Test
    void R06_mesmaRenovacaoExecutadaDuasVezesSoAcrescenta30Dias() throws Exception {
        var app = sistemaComEmprestimo();
        var executor = new ExecutorSolicitacoes(app.fachada);
        var mesma = new SolicitacaoRenovacao(UUID.randomUUID(), "E01", Instant.now(), "L01", D.plusDays(1));

        var pool = Executors.newFixedThreadPool(2);
        try {
            var largada = new CountDownLatch(1);
            Callable<Comprovante> envio = () -> {
                largada.await();
                return executor.executar(mesma);
            };
            var f1 = pool.submit(envio);
            var f2 = pool.submit(envio);
            largada.countDown(); // duas submissões simultâneas
            assertEquals(f1.get(), f2.get(), "repetição devolve o mesmo comprovante");
        } finally {
            pool.shutdownNow();
        }
        assertEquals(1, emprestimo(app).numRenovacoes());
        assertEquals(D.plusDays(60), emprestimo(app).vencimento(), "apenas +30 dias");
    }

    @Test
    void R06_solicitacoesDiferentesRespeitamLimiteDeDuasRenovacoes() {
        var app = sistemaComEmprestimo();
        app.emprestimos.renovarEmprestimo("L01", "E01", D.plusDays(1));
        app.emprestimos.renovarEmprestimo("L01", "E01", D.plusDays(2));
        assertThrows(RegraNegocioException.class, () ->
            app.emprestimos.renovarEmprestimo("L01", "E01", D.plusDays(3))
        );
        assertEquals(2, emprestimo(app).numRenovacoes());
    }

    // ===================== R07 — Template Method =====================

    @Test
    void R07_estudanteDuplicadoRetornaCadastroExistente() {
        var app = sistemaComLivro();
        app.pessoas.inserirPessoa("Bia", "Souza", "10987654321");
        Estudante existente = app.estudantes.inserirEstudante("E01", "10987654321"); // mesmo id, outro CPF
        assertEquals(new Estudante("E01", CPF), existente, "devolve o cadastro original, sem sobrescrever");
    }

    @Test
    void R07_cpfNaoCadastradoFalhaAntesDeQualquerGravacao() {
        var espia = new EspiaDeGravacoes();
        var cadastro = new CadastroEstudante(espia);
        assertThrows(RegraNegocioException.class, () -> cadastro.incluir("E02", "99999999999"));
        assertTrue(espia.gravacoes.isEmpty(), "nenhum salvar/remover foi chamado: " + espia.gravacoes);
        assertTrue(espia.real.relacionamento().estudante("E02").isEmpty());
    }

    @Test
    void R07_interfacesDeCadastroPreservadas() {
        var app = sistemaComLivro();
        iPessoa pessoas = app.pessoas;
        iLivro livros = app.livros;
        iAtendente atendentes = app.atendentes;
        assertEquals("Ana Maria", pessoas.atualizarPessoa(CPF, "Ana Maria", "Silva").nome());
        assertThrows(
            RegraNegocioException.class,
            () -> pessoas.excluirPessoa(CPF),
            "pessoa vinculada a estudante"
        );
        assertEquals("Novo título", livros.atualizarLivro("L01", "Novo título").titulo());
        atendentes.inserirAtendente("A01", CPF);
        assertEquals(CPF, atendentes.consultarAtendente("A01").cpf());
        livros.excluirLivro("L01");
        assertThrows(RegraNegocioException.class, () -> livros.consultarLivro("L01"));
    }

    // ===================== R08 — Abstract Factory =====================

    @Test
    void R08_configuracaoDeTesteForneceApenasObjetosEmMemoria() {
        var app = SistemaIB2B.inicializar("memoria", "A", "original");
        var f = app.fabrica;
        assertInstanceOf(FabricaMemoria.class, f);
        for (Object produto : List.of(
            f.livros(),
            f.movimentacao(),
            f.relacionamento(),
            f.unidadeDeTrabalho()
        )) {
            assertEquals(
                FabricaMemoria.class,
                produto.getClass().getEnclosingClass(),
                produto.getClass().getName()
            );
        }
    }

    @Test
    void R08_producaoForneceRepositoriosMySQLNaMesmaUnidadeDeTrabalho() {
        var jdbc = new JdbcFalso();
        var f = new FabricaMySQL(jdbc::abrir); // família de produção, sem servidor real
        for (Object produto : List.of(
            f.livros(),
            f.movimentacao(),
            f.relacionamento(),
            f.unidadeDeTrabalho()
        )) {
            assertEquals(
                FabricaMySQL.class,
                produto.getClass().getEnclosingClass(),
                produto.getClass().getName()
            );
        }
        jdbc.comandos.clear();
        jdbc.eventos.clear();
        int abertasAntes = jdbc.conexoesAbertas;

        f
            .unidadeDeTrabalho()
            .executar(() -> {
                f.livros().salvar(new Livro("L01", "UML"));
                f.relacionamento().salvarPessoa(new Pessoa(CPF, "Ana", "Silva"));
                f.movimentacao().salvarReserva(new Reserva("L01", "E01", D));
                return null;
            });

        assertEquals(abertasAntes + 1, jdbc.conexoesAbertas, "uma única conexão para a transação");
        assertEquals(3, jdbc.comandos.size());
        int conexao = jdbc.comandos.get(0).conexao();
        assertTrue(
            jdbc.comandos.stream().allMatch(c -> c.conexao() == conexao),
            "três repositórios, mesma conexão"
        );
        assertTrue(jdbc.comandos.get(0).sql().startsWith("INSERT INTO livros"));
        assertEquals(
            List.of(
                "conexao" + conexao + ".autoCommit=false",
                "conexao" + conexao + ".commit",
                "conexao" + conexao + ".close"
            ),
            jdbc.eventos
        );
    }

    @Test
    void R08_falhaNaTransacaoMySQLFazRollback() {
        var jdbc = new JdbcFalso();
        var f = new FabricaMySQL(jdbc::abrir);
        jdbc.eventos.clear();
        assertThrows(RegraNegocioException.class, () ->
            f
                .unidadeDeTrabalho()
                .executar(() -> {
                    f.livros().salvar(new Livro("L01", "UML"));
                    throw new RegraNegocioException("falha");
                })
        );
        assertTrue(jdbc.eventos.stream().anyMatch(e -> e.endsWith(".rollback")));
        assertTrue(jdbc.eventos.stream().noneMatch(e -> e.endsWith(".commit")));
    }

    // ===================== R09 — Decorator =====================

    @Test
    void R09_segundaConsultaUsaCacheEAindaGeraMedicao() {
        var app = sistemaComLivro();
        var acessosAoRepositorio = new int[1];
        ConsultaAcervo original = id -> {
            acessosAoRepositorio[0]++;
            return new ConsultaOriginal(app.fabrica.livros()).consultar(id);
        };
        var medicao = new ConsultaComMedicao(new ConsultaComCache(original)); // medicao(cache(original))

        assertEquals("UML aplicado", medicao.consultar("L01").orElseThrow().titulo());
        assertEquals("UML aplicado", medicao.consultar("L01").orElseThrow().titulo());

        assertEquals(2, medicao.chamadas(), "as duas consultas foram medidas");
        assertEquals(1, acessosAoRepositorio[0], "a segunda veio do cache");
    }

    @Test
    void R09_cacheAntigoNuncaAutorizaReservaDeLivroQueDeixouDeEstarLivre() {
        var app = sistemaComLivro();
        assertTrue(app.livros.consultarMetadados("L01").isPresent()); // metadados entram no cache
        app.fachada.reservar("L01", "E01", D); // livro deixa de estar LIVRE

        assertTrue(app.livros.consultarMetadados("L01").isPresent(), "cache ainda tem os metadados");
        assertThrows(RegraNegocioException.class, () -> app.fachada.reservar("L01", "E01", D.plusDays(1)));
        assertEquals(EstadoLivro.RESERVADO, estado(app));
    }

    @Test
    void R09_falhaDoCacheNaoImpedeConsulta() {
        var app = sistemaComLivro();
        Map<String, LivroInfo> cacheForaDoAr = new HashMap<>() {
            @Override
            public LivroInfo get(Object k) {
                throw new IllegalStateException("cache fora do ar");
            }

            @Override
            public LivroInfo put(String k, LivroInfo v) {
                throw new IllegalStateException("cache fora do ar");
            }
        };
        var consulta = new ConsultaComCache(new ConsultaOriginal(app.fabrica.livros()), cacheForaDoAr);
        assertEquals("UML aplicado", consulta.consultar("L01").orElseThrow().titulo());
    }

    @Test
    void R09_atualizarTituloInvalidaMetadadoEmCache() {
        var app = sistemaComLivro();
        assertEquals("UML aplicado", app.livros.consultarMetadados("L01").orElseThrow().titulo()); // entra no cache
        app.livros.atualizarLivro("L01", "UML aplicado — 2ª ed.");
        assertEquals("UML aplicado — 2ª ed.", app.livros.consultarMetadados("L01").orElseThrow().titulo());
        app.livros.excluirLivro("L01");
        assertTrue(app.livros.consultarMetadados("L01").isEmpty(), "livro excluído não volta do cache");
    }

    // ===================== R10 — Composite =====================

    @Test
    void R10_unidadeComDuasSecoesDeTresEDoisLivrosTotalizaCinco() {
        var unidade = new GrupoAcervo("Biblioteca Central");
        var computacao = unidade.novaSecao("Computação");
        var engenharia = unidade.novaSecao("Engenharia");
        for (String id : List.of("L1", "L2", "L3")) computacao.incluirLivro(id);
        for (String id : List.of("L4", "L5")) engenharia.incluirLivro(id);

        assertEquals(5, unidade.contar());
        assertEquals(3, computacao.contar());
        assertEquals(1, new ItemAcervo("L1").contar(), "item responde à mesma operação");
        assertEquals(List.of("L1", "L2", "L3", "L4", "L5"), unidade.listarIds());
    }

    @Test
    void R10_moverLivroEntreSecoesNaoAlteraIdEstadoNemEmprestimo() {
        var app = sistemaComEmprestimo();
        var unidade = new GrupoAcervo("Central");
        var s1 = unidade.novaSecao("S1");
        var s2 = unidade.novaSecao("S2");
        s1.incluirLivro("L01");
        var emprestimoAntes = emprestimo(app);
        var vencimentoAntes = emprestimoAntes.vencimento();

        s1.transferirLivro("L01", s2);

        assertEquals(List.of("L01"), s2.listarIds());
        assertEquals(0, s1.contar());
        assertEquals(EstadoLivro.EMPRESTADO, estado(app));
        assertEquals("L01", app.livros.consultarLivro("L01").id());
        assertEquals(vencimentoAntes, emprestimo(app).vencimento());
        assertFalse(emprestimo(app).encerrado());
    }

    @Test
    void R10_grupoRecusaCiclosELivroEmDoisGrupos() {
        var unidade = new GrupoAcervo("Central");
        var secao = unidade.novaSecao("Computação");
        var subsecao = secao.novaSecao("IA");
        subsecao.incluirLivro("L1");

        assertThrows(RegraNegocioException.class, () -> unidade.novaSecao("Outra").incluirLivro("L1"));
        assertThrows(RegraNegocioException.class, () -> subsecao.anexarGrupo(unidade), "ciclo indireto");
        assertThrows(RegraNegocioException.class, () -> secao.moverPara(subsecao), "mover para dentro de si");

        var externo = new GrupoAcervo("Doação");
        externo.incluirLivro("L1"); // já existe na árvore Central
        assertThrows(RegraNegocioException.class, () -> unidade.anexarGrupo(externo));

        var doacaoValida = new GrupoAcervo("Doação 2");
        doacaoValida.incluirLivro("L7");
        unidade.anexarGrupo(doacaoValida);
        assertEquals(2, unidade.contar());
        assertThrows(RegraNegocioException.class, () -> secao.incluirLivro("L7"), "L7 agora é da árvore");
    }
}
