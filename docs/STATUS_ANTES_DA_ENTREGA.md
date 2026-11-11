# Checklist antes da entrega (23/11/2026, 19h45)

## Já verificado (09/11/2026) — saídas em `docs/evidencias/`

- [x] Testes com o **JUnit 5 oficial** (junit-platform-console): **29/29 aprovados** + `MySQLIntegracaoTest` = **30/30** (`junit_29_testes_mais_mysql.txt`).
- [x] Integração com **MySQL 8.0 real**: esquema criado (8 tabelas), commit e rollback na mesma conexão.
- [x] Demo completa com `IB2B_PERFIL=mysql`, gravando no banco (`demo_mysql.txt`) e em memória (`demo_executada.txt`).
- [x] `docs/iB2B_RA03_FINAL.asta` gerado e conferido pelo exportador do Astah (23 diagramas, `docs/iB2B_RA03_FINAL.pdf`).

## Falta no computador da equipe

- [x] `mvn clean test` no Mac: BUILD SUCCESS (Tests run: 30, Failures: 0, Skipped: 1 — o MySQL só roda com IB2B_TESTAR_MYSQL=true).
- [ ] Abrir `docs/iB2B_RA03_FINAL.asta` no Astah e passar o olho nos diagramas.
- [x] Repositório Git enviado: https://github.com/gmhsss/ib2b-ra03-gof
- [ ] Ensaio: 5 minutos + cada integrante explica qualquer um dos dez padrões e faz uma extensão ao vivo (`EXTENSOES_AO_VIVO.md`).
