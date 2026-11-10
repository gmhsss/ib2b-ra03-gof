# Registro de uso de LLM — Trabalho RA03 iB2B

**Ferramentas:** ChatGPT (OpenAI), na versão inicial, e Claude (Anthropic), na revisão. Utilizadas como assistência de engenharia de software, modelagem, testes e documentação. **Data da geração inicial:** 09/11/2026.


## Solicitações principais à ferramenta

1. **Contextualização:** «Continuar os modelos do RA01/RA02 do iB2B e implementar o enunciado do RA03 em Java 17, preservando `Acervo`, `Relacionamento`, `Movimentacao`, tratadores e as sete situações de `Livro`.»
2. **Padrões:** «Aplicar separadamente State, Strategy, Adapter, Facade, Observer, Command, Template Method, Abstract Factory, Decorator e Composite, com papéis nomeados.»
3. **Testes:** «Incluir testes reproduzíveis para os dez cenários do enunciado, inclusive terceiro pedido de renovação, transação interrompida, notificação duplicada, comando simultâneo e cache obsoleto.»
4. **Modelos:** «Desenhar diagramas legíveis, incluindo o diagrama de tipos de implantação que faltou no RA02, e produzir UML de classes e sequências por padrão.»
5. **Explicação:** «Produzir README, tabela de rastreabilidade e roteiro de arguição, incluindo limitações verificadas.»

## Decisões incorporadas à versão inicial

- Estado concreto por situação do livro, com operações inválidas rejeitadas em `EstadoDoLivro` e verificação do limite antes de atualizar o vencimento.
- Uso de `LocalDate` e `BigDecimal`, evitando arredondamentos com `double`.
- Isolamento das diferenças bancárias nos adaptadores, sem condicionais por banco no serviço de multas.
- Transações com rollback da família em memória e `Connection` compartilhada na família JDBC/MySQL.
- Observer disparado após confirmação da devolução, isolando falhas de ouvintes.
- Command com identificador UUID e execução sincronizada para resistir a submissões simultâneas.
- Decorator cacheando exclusivamente metadados, com reserva conferindo o estado vivo em `Livro`.
- Composite guardando somente IDs dos livros, sem transferir suas entidades de domínio.

## Correções/rejeições efetuadas durante a elaboração

- **Corrigido:** solicitação de cobrança passou a usar o **CPF do cadastro do estudante**, em vez de enviar ao banco apenas `E01`.
- **Corrigido:** inclusão do método de encaminhamento de livro LIVRE para reparo, alinhando o caminho de domínio com a máquina de estados.
- **Corrigido:** a primeira versão JDBC armazenava tipos em uma tabela genérica; foi substituída por oito tabelas relacionais separadas com chaves estrangeiras, para refletir melhor o domínio.
- **Corrigido:** as interfaces `iLivro`, `iPessoa`, `iEstudante`, `iAtendente`, `iEmprestimo` e `iDevolucao` voltaram a expor os métodos de consulta/atualização/exclusão ou circulação do RA01/RA02, além do mínimo exigido pelos cenários GoF.
- **Descartado:** usar disponibilidade obtida do cache para autorizar reserva.
- **Descartado:** implementar Strategy com `switch` no serviço de multa ou alterar valores históricos já emitidos.
- **Descartado:** marcar JUnit ou MySQL como efetivamente validados sem que fossem executados neste ambiente.
- **Descartado:** apresentar um `.asta` do RA02 como se contivesse os dez novos diagramas do RA03.

## Revisão com Claude (Anthropic)

**Pedido:** «Verificar, com base no enunciado e nos slides de padrões, se o trabalho está completo e corrigir o que for necessário.»

**Aceito / corrigido:**

- **State:** a guarda `numRenovacoes < 2` saiu de `Emprestimo` e passou para o método `renovar` dos estados de origem (`Emprestado`, `Renovado`), como pede o alinhamento com o modelo ("guardas e ações aparecem no corpo desse método"). `Emprestimo.prorrogar()` só aplica a ação. `Acervo.excluir` deixou de testar a situação do livro com `if`; quem decide é o estado (`verificarExclusao`).
- **Adapter:** a tradução das notificações estava dentro dos bancos simulados (adaptee); passou para os adaptadores, que agora convertem valor, CPF (só dígitos × formatado), identificador e situação (`LIQ`/`pago` → `PAGA`) para `NotificacaoPagamento`.
- **Facade / separação:** a CLI acessava o repositório para listar boletos e os tratadores acessavam repositórios diretamente; agora tudo passa pelos tratadores → fachada / serviço.
- **Decorator:** a cadeia não era usada pelo sistema (só no teste). Agora é montada em `SistemaIB2B` por configuração (`IB2B_CONSULTA`) e exposta em `iLivro.consultarMetadados`. `ConsultaOriginal` ficou sem contador (implementação original sem alterações); o cache não guarda "não encontrado" e, se falhar, delega para a consulta interna; a medição passou a somar tempo.
- **Composite:** `anexarGrupo` era inutilizável (sempre falhava); agora anexa grupos de outra árvore recusando livros repetidos, e `moverPara` move seções recusando ciclos.
- **Observer:** falha de ouvinte vai para uma fila de reenvio (`reenviarFalhas`), como no material da disciplina; `remover` incluído; `OuvinteMultas` só emite com atraso.
- **Abstract Factory:** `ServicoMultas` passou a receber a fábrica inteira (não dá para misturar famílias). `FabricaMySQL` aceita uma fonte de conexões, o que permitiu testar a família de produção sem servidor (antes o teste R08 só checava se a classe existia). SQL corrigido (`FROM DUAL`; `FOR UPDATE` na leitura do boleto por notificação).
- **Testes:** os cenários estavam em `src/main` (`CenariosDeVerificacao`) e o JUnit só os chamava. Foram reescritos como 29 testes JUnit com asserções, nomeados pelo requisito (`R01_…` a `R10_…`). `Verificador` e `verificar.sh` foram removidos.
- **UML:** diagramas `.puml` atualizados para refletir o código (estereótipos dos papéis GoF, métodos, notas) e renderizados com PlantUML; PDF consolidado refeito.
- **Legibilidade:** código reformatado (antes havia classes inteiras em uma linha).

**Segunda rodada (após auditoria da equipe):**

- README deixava a entender que `iB2B_RA03_FINAL.asta` já existia; agora está marcado como **pendente**.
- Implantação: os bancos simulados aparecem como artefatos **dentro da JVM**; a API real de banco aparece como nó externo não implementado.
- Decorator: `atualizarLivro`/`excluirLivro` invalidam o metadado em cache (`ConsultaAcervo.invalidar`), com teste `R09_atualizarTituloInvalidaMetadadoEmCache`.
- Guia de arguição: explicada a limitação da idempotência do Command após reinício e como resolvê-la.

**Terceira rodada — projeto Astah:**

- O `.asta` foi gerado por um programa Java (`docs/astah_gerador/`) que usa a API oficial do Astah Professional 12 (`ProjectAccessor`, `BasicModelEditor`, editores de diagrama) sobre uma cópia do projeto do RA02. Diagrama e instâncias de implantação usam comandos internos do Astah, chamados por reflexão, porque a API pública não os cria.
- Conferência pelo exportador de linha de comando do Astah (PNG/SVG), com ajustes de posição até não haver sobreposições relevantes.

**Descartado:**

- Adicionar transições que não existem na máquina de estados do RA02 (por exemplo, cancelar reserva).

## Verificações executadas (09/11/2026)

- JUnit 5 oficial (junit-platform-console-standalone): 29/29 testes de requisitos aprovados.
- `MySQLIntegracaoTest` contra MySQL 8.0 real: aprovado. A demo com `IB2B_PERFIL=mysql` gravou corretamente nas 8 tabelas.
- Ajuste decorrente: a URL JDBC padrão passou a incluir `allowPublicKeyRetrieval=true`, exigido pela autenticação `caching_sha2_password` do MySQL 8 em conexões sem SSL.

## Verificações a cargo da equipe antes da entrega

- [ ] `mvn clean test` no computador da equipe.
- [ ] Abrir `docs/iB2B_RA03_FINAL.asta` no Astah.
- [ ] Ensaiar a demonstração de 5 minutos e a arguição (todas as integrantes explicam todos os padrões).

**Responsabilidade intelectual:** geração por LLM não substitui revisão, compreensão e manutenção pela equipe.
