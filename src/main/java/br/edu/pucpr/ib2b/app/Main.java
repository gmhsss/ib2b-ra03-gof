package br.edu.pucpr.ib2b.app;

import br.edu.pucpr.ib2b.dominio.*;
import br.edu.pucpr.ib2b.padroes.*;
import java.time.*;
import java.util.*;

/**
 * Interface de linha de comando. Chama SOMENTE os tratadores (app.pessoas, app.livros,
 * app.emprestimos, app.devolucoes...) e imprime comprovantes; não contém regra de domínio.
 */
public final class Main {

    private Main() {}

    public static void main(String[] args) {
        String perfil = System.getenv().getOrDefault("IB2B_PERFIL", "memoria");
        String banco = System.getenv().getOrDefault("IB2B_BANCO", "A");
        String politica = System.getenv().getOrDefault("IB2B_POLITICA", "original");
        String consulta = System.getenv().getOrDefault("IB2B_CONSULTA", "medicao,cache");
        var app = SistemaIB2B.inicializar(perfil, banco, politica, consulta);
        System.out.printf(
            "Perfil=%s | Banco=%s | Política=%s | Consulta=%s%n",
            perfil,
            banco,
            politica,
            consulta
        );
        if (Arrays.asList(args).contains("--interativo")) interativo(app);
        else demo(app);
    }

    /** Fluxo completo exigido na demonstração: reserva, empréstimo, renovação, devolução atrasada e multa. */
    public static void demo(SistemaIB2B app) {
        var d = LocalDate.of(2026, 11, 9);
        // IDs únicos: a demo pode ser repetida no MySQL sem colidir com execuções anteriores
        var sufixo = UUID.randomUUID().toString().substring(0, 8);
        var idLivro = "L-" + sufixo;
        var idEstudante = "E-" + sufixo;
        var cpf = String.format(
            "%011d",
            Math.floorMod(UUID.randomUUID().getMostSignificantBits(), 100_000_000_000L)
        );

        System.out.println("=== iB2B | Demonstração funcional ===");
        app.pessoas.inserirPessoa("Ana", "Silva", cpf);
        app.estudantes.inserirEstudante(idEstudante, cpf);
        app.livros.inserirLivro(idLivro, "Engenharia de Software");
        System.out.println("1. Cadastro: pessoa, estudante " + idEstudante + " e livro " + idLivro);

        // Command: a tela cria a solicitação e a entrega ao tratador/executor
        var reserva = new SolicitacaoReserva(UUID.randomUUID(), idEstudante, Instant.now(), idLivro, d);
        System.out.println("2. " + app.emprestimos.processar(reserva));
        System.out.println("3. " + app.emprestimos.efetivarEmprestimo(idLivro, idEstudante, d));
        var renov1 = new SolicitacaoRenovacao(
            UUID.randomUUID(),
            idEstudante,
            Instant.now(),
            idLivro,
            d.plusDays(1)
        );
        System.out.println("4. " + app.emprestimos.processar(renov1));
        System.out.println("   (mesma solicitação reenviada) " + app.emprestimos.processar(renov1));
        var renov2 = new SolicitacaoRenovacao(
            UUID.randomUUID(),
            idEstudante,
            Instant.now(),
            idLivro,
            d.plusDays(2)
        );
        System.out.println("5. " + app.emprestimos.processar(renov2));
        try {
            app.emprestimos.renovarEmprestimo(idLivro, idEstudante, d.plusDays(3));
        } catch (RegraNegocioException e) {
            System.out.println("6. Terceira renovação recusada: " + e.getMessage());
        }
        // vencimento = d + 30 + 30 + 30 = d + 90; devolvido em d + 95 => 5 dias de atraso
        System.out.println("7. " + app.devolucoes.registrarDevolucao(idLivro, idEstudante, d.plusDays(95)));
        System.out.println(
            "   Estado do livro: " + app.livros.consultarLivro(idLivro).estado() + " (aguarda inspeção)"
        );
        app.devolucoes
            .consultarMulta(idLivro, idEstudante)
            .ifPresentOrElse(
                b ->
                    System.out.printf(
                        "8. Multa %s | R$ %s | política %s | cobrança no banco: %s%n",
                        b.codigo(),
                        b.valor(),
                        b.politica(),
                        b.codigoExterno()
                    ),
                () -> System.out.println("8. Sem multa")
            );
        System.out.println("9. " + app.devolucoes.inspecionar(idLivro, true, d.plusDays(95)));
    }

    private static void interativo(SistemaIB2B app) {
        Scanner sc = new Scanner(System.in);
        System.out.println("iB2B | modo interativo, datas AAAA-MM-DD");
        while (true) {
            System.out.println(
                "1 Pessoa | 2 Estudante | 3 Livro | 4 Reserva | 5 Empréstimo | 6 Renovação | 7 Devolução" +
                " | 8 Inspeção | 9 Multa | 10 Pagamento | 11 Concluir reparo | 12 Laudo irrecuperável" +
                " | 13 Consultar livro | 0 Sair"
            );
            if (!sc.hasNextLine()) return;
            var opcao = sc.nextLine().trim();
            if (opcao.equals("0")) return;
            try {
                switch (opcao) {
                    case "1" -> {
                        System.out.print("CPF Nome Sobrenome: ");
                        var t = sc.nextLine().trim().split("\\s+");
                        System.out.println(app.pessoas.inserirPessoa(t[1], t[2], t[0]));
                    }
                    case "2" -> {
                        System.out.print("ID CPF: ");
                        var t = sc.nextLine().trim().split("\\s+");
                        System.out.println(app.estudantes.inserirEstudante(t[0], t[1]));
                    }
                    case "3" -> {
                        System.out.print("ID: ");
                        var id = sc.nextLine().trim();
                        System.out.print("Título: ");
                        System.out.println(app.livros.inserirLivro(id, sc.nextLine().trim()).titulo());
                    }
                    case "4", "5", "6", "7" -> {
                        System.out.print("ID livro, ID estudante, data: ");
                        var t = sc.nextLine().trim().split(",\\s*");
                        var data = LocalDate.parse(t[2]);
                        var comprovante =
                            switch (opcao) {
                                case "4" -> app.emprestimos.processar(
                                    new SolicitacaoReserva(UUID.randomUUID(), t[1], Instant.now(), t[0], data)
                                );
                                case "5" -> app.emprestimos.efetivarEmprestimo(t[0], t[1], data);
                                case "6" -> app.emprestimos.processar(
                                    new SolicitacaoRenovacao(
                                        UUID.randomUUID(),
                                        t[1],
                                        Instant.now(),
                                        t[0],
                                        data
                                    )
                                );
                                default -> app.devolucoes.registrarDevolucao(t[0], t[1], data);
                            };
                        System.out.println(comprovante);
                    }
                    case "8" -> {
                        System.out.print("ID livro, apto (true/false), data: ");
                        var t = sc.nextLine().trim().split(",\\s*");
                        System.out.println(
                            app.devolucoes.inspecionar(
                                t[0],
                                Boolean.parseBoolean(t[1]),
                                LocalDate.parse(t[2])
                            )
                        );
                    }
                    case "9" -> {
                        System.out.print("ID livro, ID estudante: ");
                        var t = sc.nextLine().trim().split(",\\s*");
                        System.out.println(
                            app.devolucoes.consultarMulta(t[0], t[1]).map(Main::descrever).orElse("Sem multa")
                        );
                    }
                    case "10" -> {
                        System.out.print(
                            "Notificação do banco (ex.: LIQ|A-... ou status=pago;boleto=B/...): "
                        );
                        boolean quitou = app.devolucoes.registrarPagamento(sc.nextLine().trim());
                        System.out.println(quitou ? "Multa quitada" : "Notificação repetida/ignorada");
                    }
                    case "11", "12" -> {
                        System.out.print("ID livro, data: ");
                        var t = sc.nextLine().trim().split(",\\s*");
                        var data = LocalDate.parse(t[1]);
                        System.out.println(
                            opcao.equals("11")
                                ? app.devolucoes.concluirReparo(t[0], data)
                                : app.devolucoes.laudoIrrecuperavel(t[0], data)
                        );
                    }
                    case "13" -> {
                        System.out.print("ID livro: ");
                        var id = sc.nextLine().trim();
                        System.out.println(
                            app.livros.consultarMetadados(id).map(Object::toString).orElse("Não encontrado") +
                            " | estado atual: " +
                            app.livros.consultarLivro(id).estado()
                        );
                    }
                    default -> System.out.println("Opção inválida");
                }
            } catch (RuntimeException e) {
                System.out.println("Falha: " + e.getMessage());
            }
        }
    }

    private static String descrever(BoletoMulta b) {
        return String.format(
            "Multa %s | R$ %s | política %s | banco %s | %s",
            b.codigo(),
            b.valor(),
            b.politica(),
            b.codigoExterno(),
            b.quitado() ? "QUITADA" : "EM ABERTO"
        );
    }
}
