# Prática: extensões de padrão ao vivo

O professor pode pedir uma extensão pequena, não uma refatoração completa. Duas opções seguras para treinar:

## Opção A — Novo ouvinte de devolução (Observer)

Crie `src/main/java/br/edu/pucpr/ib2b/padroes/OuvinteAuditoria.java` com:

```java
package br.edu.pucpr.ib2b.padroes;
public final class OuvinteAuditoria implements OuvinteDevolucao {
    @Override public void aoDevolver(EventoDevolucao evento) {
        System.out.println("AUDITORIA: " + evento.idLivro());
    }
}
```

Em `app/SistemaIB2B.java`, perto de `publicador.registrar(inspecao);` adicione:

```java
publicador.registrar(new OuvinteAuditoria());
```

Execute `mvn -q test`. A fachada, o publicador e os demais ouvintes **não** precisam mudar.

## Opção B — Nova política de multa (Strategy)

Crie `src/main/java/br/edu/pucpr/ib2b/padroes/MultaDoisReais.java`:

```java
package br.edu.pucpr.ib2b.padroes;
import java.math.BigDecimal;
public final class MultaDoisReais implements PoliticaMulta {
    public String nome() { return "DOBRO"; }
    public BigDecimal calcular(long dias) {
        return BigDecimal.valueOf(Math.max(0, dias)).multiply(new BigDecimal("2.00"));
    }
}
```

No `SistemaIB2B.inicializar()`, no `switch` de políticas, adicione `case "dobro" -> new MultaDoisReais();`. Configure com `export IB2B_POLITICA=dobro`. O `ServicoMultas` e a classe de boleto permanecem inalterados.

**Explicação esperada:** o padrão isola comportamento variável em classe própria e o restante do código depende de interface. A composição escolhe a implementação; a regra central não é alterada.
