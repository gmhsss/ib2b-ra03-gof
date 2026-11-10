# Projeto Astah do RA03

`docs/iB2B_RA03_FINAL.asta` já contém os modelos do RA03, gerados pela API oficial do Astah Professional 12 a partir de `docs/base_RA02.asta` (os pacotes RA01/RA02 continuam lá).

Pacote **RA03 — Padrões GoF**:

- `Modelo (código Java)` — as classes do código (dominio, estado, persistencia, servico, padroes, app), cada uma uma única vez;
- `1 Estados` — máquina de estados de Livro (7 estados, guardas e ações);
- `2 Classes por padrão` — R01 a R10;
- `3 Sequências` — SEQ R01 a SEQ R10;
- `4 Implantação` — tipos e instâncias.

## Antes de entregar

1. Abrir o `.asta` no Astah e conferir os 23 diagramas (a verificação automática foi feita pelo exportador de imagens, sem a interface).
2. Se mexer em algo, salvar e exportar o PDF de novo pelo Astah (`docs/iB2B_RA03_FINAL.pdf`).
3. Para regenerar do zero: `docs/astah_gerador/gerar.sh` (usa a API do Astah instalado; a parte de implantação usa comandos internos do Astah 12 e pode quebrar em outra versão).
