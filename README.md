# iB2B • RA03 — Implementação Java 17 com dez padrões GoF

**PUCPR • Bacharelado em Ciência da Computação**
**Modelagem de Sistemas Computacionais — Prof. Edson Emílio Scalabrin • 2026/2**
**Equipe:** Rafaella Somoza e Gabriela Sofia

Continuação dos trabalhos RA01/RA02. O sistema executa **cadastro → reserva → empréstimo → até duas renovações → devolução → multa → inspeção** em **Java 17 + Maven**, com interface **CLI**, testes **JUnit 5** e duas famílias de persistência (**memória** para testes e **MySQL/JDBC** para produção). A CLI chama apenas os tratadores; os tratadores delegam à fachada; as regras ficam no domínio.

## Como compilar, testar e executar

Pré-requisitos: **JDK 17+** e **Maven 3.9+** (na primeira execução o Maven baixa as dependências).

```bash
mvn clean test                                  # compila e roda os testes (cenários R01–R10)
mvn -q compile exec:java -Dexec.args="--demo"           # fluxo completo pela CLI
mvn -q compile exec:java -Dexec.args="--interativo"     # menu interativo
./demo.sh                                       # os dois primeiros comandos em sequência
```

### Perfil de produção (MySQL)

```bash
docker compose up -d                            # MySQL 8 local (credenciais só de desenvolvimento)
export IB2B_PERFIL=mysql
export IB2B_DB_URL='jdbc:mysql://localhost:3306/ib2b?allowPublicKeyRetrieval=true' IB2B_DB_USER=ib2b IB2B_DB_PASSWORD=ib2b
mvn -q compile exec:java -Dexec.args="--demo"
IB2B_TESTAR_MYSQL=true mvn test                 # inclui o teste de integração com o servidor real
```

O esquema (8 tabelas InnoDB com chaves estrangeiras e `DECIMAL(12,2)` para multas) é criado automaticamente. O Oracle continua apenas como **réplica** do modelo arquitetural do RA02; a aplicação não grava nele.

### Configuração administrativa (variáveis de ambiente)

| Variável | Valores | Padrão | Padrão GoF afetado |
|---|---|---|---|
| `IB2B_PERFIL` | `memoria`, `mysql` | `memoria` | Abstract Factory |
| `IB2B_BANCO` | `A`, `B` | `A` | Adapter |
| `IB2B_POLITICA` | `original`, `teto`, `tolerancia` | `original` | Strategy |
| `IB2B_CONSULTA` | lista de decoradores, de fora para dentro (`medicao,cache`, `cache`, ``) | `medicao,cache` | Decorator |

Tudo é escolhido **uma única vez** em `app/SistemaIB2B.java`, o único ponto que conhece classes concretas.

## Estrutura

```text
src/main/java/br/edu/pucpr/ib2b/
  dominio/       Livro, Emprestimo, Reserva, Devolucao, BoletoMulta, Pessoa, Estudante, Atendente
  estado/        EstadoDoLivro + 7 estados concretos (State)
  persistencia/  FabricaPersistencia, FabricaMemoria, FabricaMySQL, repositórios, UnidadeDeTrabalho
  servico/       Acervo, Relacionamento, Movimentacao, CirculacaoFacade, ServicoMultas,
                 tratadores e interfaces iLivro, iPessoa, iEstudante, iAtendente, iEmprestimo, iDevolucao
  padroes/       Strategy, Adapter, Observer, Command, Template Method, Decorator, Composite
  app/           SistemaIB2B (montagem) e Main (CLI)
src/test/java/   RequisitosGoFTest (29 testes, prefixo R01–R10) + MySQLIntegracaoTest (opcional)
docs/            rastreabilidade, roteiro, arguição, Astah, UML (PlantUML, SVG e PDF)
```

## Padrões e cenários de verificação

| Req. | Padrão | Classes principais | Testes (prefixo do requisito) |
|---|---|---|---|
| R01 | State | `Livro` → `EstadoDoLivro` → `Livre`, `Reservado`, `Emprestado`, `Renovado`, `Devolvido`, `Reparando`, `Incinerado` | `R01_terceiraRenovacaoRecusada`, `R01_devolvidoNaoInspecionadoNaoPodeSerReservado`, … |
| R02 | Strategy | `PoliticaMulta`, `MultaOriginal`, `MultaComTeto`, `MultaComTolerancia`, `ServicoMultas` | `R02_cincoDiasDeAtrasoNasPoliticas`, `R02_trocaDePoliticaNaoRecalculaMultaEmitida` |
| R03 | Adapter | `GatewayCobranca`, `AdaptadorBancoA/B`, `BancoA/BSimulado` | `R03_mesmaSolicitacaoGera700CentavosNoAe7ReaisNoB`, `R03_notificacaoRepetidaNaoGeraSegundaQuitacao` |
| R04 | Facade | `CirculacaoFacade`, `Acervo`, `Relacionamento`, `Movimentacao` | `R04_efetivacaoValidaReserva…`, `R04_falhaNoMeioDaEfetivacaoDesfazTudo` |
| R05 | Observer | `PublicadorDevolucao`, `OuvinteDevolucao` + 4 ouvintes | `R05_devolucaoAtrasadaAlimenta…`, `R05_devolucaoNoPrazoNaoGeraMulta`, `R05_falhaDoEmail…` |
| R06 | Command | `SolicitacaoCirculacao`, `SolicitacaoReserva/Renovacao`, `ExecutorSolicitacoes` | `R06_mesmaRenovacaoExecutadaDuasVezesSoAcrescenta30Dias`, `R06_solicitacoesDiferentes…` |
| R07 | Template Method | `CadastroAbstrato<T>.incluir()` (final) + 4 cadastros | `R07_estudanteDuplicadoRetornaCadastroExistente`, `R07_cpfNaoCadastradoFalhaAntesDeQualquerGravacao` |
| R08 | Abstract Factory | `FabricaPersistencia`, `FabricaMemoria`, `FabricaMySQL` | `R08_configuracaoDeTesteForneceApenasObjetosEmMemoria`, `R08_producaoForneceRepositoriosMySQLNaMesmaUnidadeDeTrabalho` |
| R09 | Decorator | `ConsultaAcervo`, `ConsultaOriginal`, `ConsultaComCache`, `ConsultaComMedicao` | `R09_segundaConsultaUsaCacheEAindaGeraMedicao`, `R09_cacheAntigoNuncaAutoriza…` |
| R10 | Composite | `ElementoAcervo`, `GrupoAcervo`, `ItemAcervo` | `R10_unidadeComDuasSecoes…TotalizaCinco`, `R10_moverLivroEntreSecoes…` |

A tabela completa (requisito → papel de cada classe → teste → diagrama) está em [`docs/RASTREABILIDADE.md`](docs/RASTREABILIDADE.md).

Decisões de domínio: a guarda `numRenovacoes < 2` fica no método `renovar` do estado de origem (`Emprestado`/`Renovado`) e é avaliada **antes** de prorrogar o vencimento; datas usam `LocalDate`/`ChronoUnit.DAYS`; dinheiro usa `BigDecimal` com duas casas; a multa grava o valor e o nome da política na emissão.

## Modelos UML

- `docs/uml/MODELOS_UML_RA03.pdf` — capa, máquina de estados de Livro, 10 diagramas de classes (um por padrão), 10 diagramas de sequência dos cenários e implantação (tipos e instâncias).
- `docs/uml/*.puml` — fontes PlantUML editáveis; `*.svg`/`*.pdf` gerados a partir delas.
- `docs/iB2B_RA03_FINAL.asta` — **projeto Astah** com o RA01/RA02 e o novo pacote **"RA03 — Padrões GoF"**: máquina de estados de Livro, 10 diagramas de classes (um por padrão), 10 diagramas de sequência e implantação (tipos e instâncias), todos com elementos UML nativos. `docs/iB2B_RA03_FINAL.pdf` é a exportação desses diagramas pelo exportador do próprio Astah.
- `docs/astah_gerador/` — programa que monta esse pacote pela API oficial do Astah (`gerar.sh`); `docs/base_RA02.asta` é o projeto de partida.

## Uso de LLMs

- **ChatGPT (OpenAI)** — geração da versão inicial do código, testes, diagramas e documentação.
- **Claude (Anthropic)** — revisão da versão inicial contra o enunciado e correções (detalhes em `REGISTRO_LLM.md`).

O registro dos pedidos e do que foi aceito, corrigido ou descartado está em [`REGISTRO_LLM.md`](REGISTRO_LLM.md). A equipe é responsável por todo o código entregue.

## Entrega

Repositório até **23/11/2026, 19h45**; demonstração no mesmo dia (5 min). `target/` fica fora do repositório (`.gitignore`).
