# Arguição individual — respostas que precisam sair sem consulta

O professor pode apontar qualquer classe e pedir explicação e alteração. **Cada integrante precisa compreender os dez padrões**, mesmo que tenha escrito apenas alguns módulos. A nota individual da equipe é multiplicada pelo fator indicado no enunciado: **1,0** (explica com segurança), **0,7** (lacunas), **0,4** (só sua parte), **0** (não explica).

## R01 — State

- **Onde:** `dominio/Livro.java`, `estado/EstadoDoLivro.java`, `estado/{Livre,Reservado,Emprestado,Renovado,Devolvido,Reparando,Incinerado}.java`.
- **Explicação:** o contexto `Livro` delega cada evento ao estado atual. Não existe um `switch(estado)` nos tratadores.
- **Guarda:** em `estado/Emprestado.java` e `estado/Renovado.java`, método `renovar(livro, emprestimo)`: `if (e.numRenovacoes() >= 2) throw` **antes** de `e.prorrogar()` e de `alterarEstado(RENOVADO)`. Se a guarda falha, nada muda (e a UoW ainda faz rollback).
- **Evento num estado que não aceita:** o método `default` de `EstadoDoLivro` lança `RegraNegocioException` sem tocar no livro.
- **Persistência:** guarda o enum `EstadoLivro` em cada registro e reconstitui a classe concreta via `Estados.doTipo` ao ler.
- **Se alterar:** permitir terceira renovação viola o limite; retirar operação inválida pode criar transições de estados erradas.

## R02 — Strategy

- **Onde:** `padroes/PoliticaMulta` + três políticas e `servico/ServicoMultas`.
- **Explicação:** `ServicoMultas` chama `politica.calcular(dias)` sem saber a fórmula concreta; configuração muda qual objeto injeta.
- **Histórico:** `BoletoMulta` armazena `valor` e `politica` imutáveis na criação; mudar a estratégia depois não recalcula boletos existentes.
- **Dias e moeda:** `ChronoUnit.DAYS`/`LocalDate`; `BigDecimal` para evitar erro de ponto flutuante.
- **Mudança ao vivo:** criar uma classe `PoliticaMulta` nova, sem modificar `ServicoMultas`.

## R03 — Adapter

- **Onde:** `GatewayCobranca`, `AdaptadorBancoA`, `AdaptadorBancoB`, APIs simuladas.
- **Explicação:** o serviço de multas só conhece `GatewayCobranca`. O A pede centavos inteiros e o B pede reais decimais.
- **Conversão (tudo nos adaptadores):** valor — R$ 7,00 vira `700` no A (`movePointRight(2).intValueExact()`) e `7.00` no B; CPF — só dígitos no A, `000.000.000-00` no B; identificador — alfanumérico no A; situação — `LIQ|…` (A) e `status=pago;…` (B) viram `NotificacaoPagamento(…, PAGA)`.
- **Escolha do banco:** `IB2B_BANCO` lido em `SistemaIB2B.inicializar`; `ServicoMultas` só vê `GatewayCobranca`.
- **Avisos repetidos:** `BoletoMulta.quitar()` muda `quitado` apenas uma vez; segunda notificação não gera segunda quitação.
- **Mudança ao vivo:** incluir Banco C criando outro adaptador, sem mexer nas regras de multas.

## R04 — Facade

- **Onde:** `servico/CirculacaoFacade.java`, `Acervo`, `Movimentacao`, `Relacionamento`, `UnidadeDeTrabalho`.
- **Explicação:** a fachada valida estudante, confere reserva, altera livro, remove reserva e registra empréstimo em uma transação; devolve `Comprovante`.
- **Falha:** a UoW restaura snapshot no perfil memória; JDBC usa rollback. O R04 injeta falha após remover a reserva e verifica que ela voltou.
- **Não expor coleções:** a CLI só usa tratadores que delegam para a fachada.

## R05 — Observer

- **Onde:** `PublicadorDevolucao` e `OuvinteInspecao`, `OuvinteMultas`, `OuvinteIndicadores`, `OuvinteEmail`.
- **Explicação:** após commit da devolução, publica `EventoDevolucao`, com vencimento, data efetiva e estudante. Cada ouvinte recebe dados suficientes.
- **E-mail falha:** `PublicadorDevolucao` captura a exceção (try/catch por ouvinte), guarda em `falhas()` (fila de reenvio, `reenviarFalhas()`) e continua notificando os demais; a devolução já foi confirmada antes de publicar, então não é desfeita.
- **Por que o evento leva os dados:** modelo *push* — o ouvinte não consulta o banco depois, quando os dados já podem ter mudado (ex.: livro já inspecionado).
- **Mudança ao vivo:** criar `OuvinteAuditoria` e registrá-lo na composição, sem mexer em `CirculacaoFacade` nem no publicador.

## R06 — Command

- **Onde:** `SolicitacaoCirculacao`, `SolicitacaoReserva`, `SolicitacaoRenovacao`, `ExecutorSolicitacoes`.
- **Papéis:** interface/command, comandos concretos, `ExecutorSolicitacoes` como invoker, `CirculacaoFacade` como receiver, CLI como client.
- **Idempotência:** `synchronized` serializa o trecho consulta+execução+memorização por UUID. Repetição retorna o comprovante anterior.
- **Limitação (saber explicar):** o mapa `UUID → comprovante` fica na memória do `ExecutorSolicitacoes`. Enquanto a aplicação roda, repetição (até simultânea) não executa duas vezes; **após reiniciar**, o mapa é perdido. Em produção, a solução seria gravar o UUID numa tabela com chave única na mesma transação da operação (ex.: `solicitacoes_executadas`), de modo que a segunda gravação falhe e a repetição seja detectada também entre reinícios e entre instâncias. Mesmo sem isso, a regra de domínio (máximo de duas renovações) continua protegida pelo State.
- **Por que sem undo:** devolver ou reservar desencadeia mudanças de domínio e efeitos externos que não são automaticamente reversíveis por comando; falhas intermediárias são tratadas por transação, não Undo.

## R07 — Template Method

- **Onde:** `CadastroAbstrato<T>` método `final incluir(String...)`; quatro subclasses de cadastros.
- **Fluxo fixo:** buscar → validar → verificar pré-requisitos → criar → armazenar → apresentar.
- **Ganchos:** `verificarPreRequisitos` e `apresentar` têm implementações padrão opcionais; a classe de estudante sobrepõe a checagem do CPF.
- **Falha sem gravação:** a verificação de CPF ocorre antes da criação e armazenamento; a UoW ainda protege o procedimento.

## R08 — Abstract Factory

- **Onde:** `FabricaPersistencia`, `FabricaMemoria`, `FabricaMySQL`, três `...Repositorio`, `UnidadeDeTrabalho`.
- **Explicação:** uma configuração seleciona uma família inteira de persistência, com os repositórios e a UoW compatíveis; não se mistura memória e JDBC na mesma instalação.
- **Transação MySQL:** `FabricaMySQL` usa uma `Connection` compartilhada por thread durante a UoW, com `setAutoCommit(false)`, `commit`/`rollback`.
- **Réplica:** Oracle é só premissa arquitetural herdada; a aplicação não grava nela.
- **Teste sem servidor:** `R08_producaoForneceRepositoriosMySQLNaMesmaUnidadeDeTrabalho` usa um JDBC falso (`JdbcFalso`) e prova que os três repositórios usam a MESMA conexão, com um único commit. `MySQLIntegracaoTest` faz o mesmo com servidor real.
- **O que impede misturar famílias:** os serviços recebem a fábrica inteira (ex.: `new ServicoMultas(fabrica, …)`), nunca repositórios avulsos.

## R09 — Decorator

- **Onde:** `ConsultaAcervo`, `ConsultaOriginal`, `DecoradorConsulta`, `ConsultaComCache`, `ConsultaComMedicao`.
- **Explicação:** cliente usa o mesmo contrato; cada decoração agrega um comportamento sem alterar a implementação original.
- **Ordem:** `Medição(Cache(Original))` registra chamadas mesmo quando cache acerta; invertendo a ordem, medição pode contar apenas cache misses.
- **Título alterado:** `TratadorDeAcervo.atualizarLivro`/`excluirLivro` chamam `consulta.invalidar(id)`, que percorre a cadeia; o cache remove a entrada obsoleta.
- **Montagem:** `SistemaIB2B.montarConsulta(repo, "medicao,cache")` (variável `IB2B_CONSULTA`).
- **Se o cache falhar:** `ConsultaComCache` trata a exceção como *miss* e delega à consulta interna.
- **Segurança:** cache guarda só `LivroInfo(id,titulo)`; a reserva lê o `Livro` atual pelo `Acervo` na fachada, nunca o cache.

## R10 — Composite

- **Onde:** `ElementoAcervo`, `GrupoAcervo`, `ItemAcervo`.
- **Explicação:** grupos e folhas têm a mesma interface `contar()`/`listarIds()`. A contagem é recursiva.
- **Integridade:** `donoDeLivro` (registro compartilhado pela árvore) impede um ID em dois grupos; `ehAncestralDe` impede ciclos em `anexarGrupo` e `moverPara`.
- **Movimento:** troca a referência ao ID do livro; não muda `Livro`, `Emprestimo` ou qualquer estado de circulação.

## Perguntas de arquitetura

**Qual a diferença entre padrão e framework?** Padrão é solução de projeto reutilizável; aqui cada padrão é aplicado em classes Java concretas, não é um framework pronto.

**Por que não usar `enum` com `switch` para todas as transições?** Isso concentraria os eventos do ciclo de vida em condicionais e retiraria o papel de cada estado concreto exigido pelo State.

**Por que usar `BigDecimal`?** Cálculos monetários com `double` podem perder precisão decimal; `BigDecimal` define valores exatos e escala.

**Como garantir que mudança em UI não quebre o domínio?** A UI só conhece interfaces/tratadores; as regras de negócio estão encapsuladas em domínio, serviços e padrões.

**Como testou rollback?** `R04_falhaNoMeioDaEfetivacaoDesfazTudo` usa um gancho de falha controlada entre etapas do empréstimo e verifica que estado, reserva e movimentação não foram alterados.


