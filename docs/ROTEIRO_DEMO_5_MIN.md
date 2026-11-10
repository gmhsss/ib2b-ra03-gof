# Roteiro de demonstração — máximo 5 minutos

O enunciado do RA03 determina **demonstrações de 5 minutos por equipe em 23/11/2026**. Não é a apresentação de 6 minutos do RA02. O professor também fará arguição individual e pode pedir uma modificação ao vivo. O objetivo é deixar os **testes já instalados**, o Terminal e as classes principais abertos antes da demonstração.

## 0:00–0:30 | Contexto

"Bom dia, professor. Esse é o RA03 do sistema iB2B, dando continuidade aos modelos do RA01 e RA02. Implementamos em Java 17 as operações de cadastro e circulação com os dez padrões GoF exigidos. Mantivemos os tratadores e os pontos internos Acervo, Movimentacao e Relacionamento." 

## 0:30–1:20 | Mostrar verificações

No Terminal, dentro da pasta do projeto, executar **`mvn clean test`**.

"Aqui estão os testes com prefixo R01 a R10, cada um verificando um cenário do enunciado. Além do fluxo normal, testamos terceira renovação, notificação bancária duplicada, falha transacional, falha isolada de e-mail e dois comandos simultâneos." 

## 1:20–2:30 | Fluxo completo na CLI

Executar **`mvn -q compile exec:java -Dexec.args="--demo"`**.

"O fluxo começa cadastrando pessoa, estudante e livro. Em seguida, reservamos e efetivamos o empréstimo. O State permite duas renovações de 30 dias, e bloqueia a terceira antes de alterar a data. Devolvemos o livro cinco dias após o vencimento, então a Strategy original calcula multa de cinco reais; o Adapter registra a cobrança no banco escolhido, sem o serviço de multas conhecer a API. O Observer aciona a inspeção, a multa e os indicadores; o reenvio da mesma solicitação de renovação (Command) devolve o mesmo comprovante. Após inspecionar, o livro volta a LIVRE." 

## 2:30–3:35 | Mostrar a arquitetura e os dez padrões

Abra o PDF `docs/uml/MODELOS_UML_RA03.pdf` e percorra as páginas de classes, ou mantenha o arquivo aberto e cite as classes-chave.

"Para separar responsabilidades, usamos a Facade na circulação, o Command nas solicitações com UUID, o Template Method nos cadastros, e a Abstract Factory para selecionar a família de repositórios. O Decorator adiciona cache e medição sem modificar a consulta original. O Composite organiza o acervo em uma árvore de seções e itens. Essas decisões estão ligadas aos diagramas UML e à tabela de rastreabilidade." 

## 3:35–4:25 | Abrir três trechos do código

- `estado/Renovado.java`: destacar a guarda `numRenovacoes >= 2` **antes** de `e.prorrogar()`.
- `servico/CirculacaoFacade.java`: mostrar `uow.executar(...)`, validação da reserva e `publicador.publicar(...)` **depois** da transação.
- `padroes/ExecutorSolicitacoes.java`: mostrar `synchronized` e `computeIfAbsent` por UUID.

"Esses trechos demonstram regras, transações e proteção contra reenvio simultâneo." 

## 4:25–5:00 | Encerramento e preparação para perguntas

"Entregamos o código Java, os testes, README, matriz de rastreabilidade e modelos UML por padrão. As versões de implantação por tipos e instâncias estão separadas e com melhor legibilidade, corrigindo o ponto observado no RA02. Obrigada." 

### Operação prática

No Terminal do Mac:

```bash
cd /caminho/para/RA03_iB2B_Java17
mvn clean test
mvn -q compile exec:java -Dexec.args="--demo"
```

Não gaste tempo abrindo cada diagrama individualmente durante a demonstração. Deixe o PDF e o editor de código prontos em abas. O tempo é estimado; ensaie até caber em cinco minutos. A equipe deve saber responder **todos** os padrões na arguição, mesmo quando somente uma pessoa fala na apresentação.
