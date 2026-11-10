package br.edu.pucpr.ib2b.servico;

import br.edu.pucpr.ib2b.dominio.*;
import br.edu.pucpr.ib2b.padroes.*;
import br.edu.pucpr.ib2b.persistencia.*;
import java.util.*;

/**
 * Strategy — Context: usa a PoliticaMulta vigente sem saber qual é.
 * Adapter — Client: conversa só com GatewayCobranca, sem nenhum "if banco A / banco B".
 */
public final class ServicoMultas {

    private final MovimentacaoRepositorio repo;
    private final RelacionamentoRepositorio pessoas;
    private final UnidadeDeTrabalho uow;
    private final GatewayCobranca banco;
    private volatile PoliticaMulta politica;

    /** Recebe a FÁBRICA inteira: repositórios e UoW vêm sempre da mesma família. */
    public ServicoMultas(FabricaPersistencia fabrica, GatewayCobranca banco, PoliticaMulta politica) {
        this.repo = fabrica.movimentacao();
        this.pessoas = fabrica.relacionamento();
        this.uow = fabrica.unidadeDeTrabalho();
        this.banco = Objects.requireNonNull(banco);
        this.politica = Objects.requireNonNull(politica);
    }

    /** Troca administrativa da política: vale para as PRÓXIMAS emissões; boletos já emitidos não mudam. */
    public void configurarPolitica(PoliticaMulta nova) {
        this.politica = Objects.requireNonNull(nova);
    }

    public PoliticaMulta politicaVigente() {
        return politica;
    }

    public static String codigoDa(Devolucao d) {
        return "M-" + d.idLivro() + "-" + d.dataDevolucao();
    }

    public Optional<BoletoMulta> emitir(Devolucao d) {
        return uow.executar(() -> {
            var existente = repo.boleto(codigoDa(d));
            if (existente.isPresent()) return existente; // emissão idempotente
            var aplicada = politica; // captura a política no instante da emissão
            var valor = aplicada.calcular(d.diasAtraso());
            if (valor.signum() == 0) return Optional.<BoletoMulta>empty();
            var cpf = pessoas
                .estudante(d.idEstudante())
                .orElseThrow(() -> new RegraNegocioException("Estudante inválido"))
                .cpf();
            var externo = banco.emitir(codigoDa(d), valor, cpf);
            var boleto = new BoletoMulta(
                codigoDa(d),
                d.idLivro(),
                d.idEstudante(),
                aplicada.nome(),
                valor,
                d.dataDevolucao(),
                externo,
                false
            );
            repo.salvarBoleto(boleto); // grava valor E nome da política aplicada
            return Optional.of(boleto);
        });
    }

    /**
     * Recebe a notificação crua do banco. O adaptador traduz; aqui só há regra institucional.
     * @return true apenas na PRIMEIRA quitação; notificação repetida devolve false e não quita de novo.
     */
    public boolean registrarNotificacao(String notificacaoDoBanco) {
        NotificacaoPagamento n = banco.traduzirNotificacao(notificacaoDoBanco);
        return uow.executar(() -> {
            var boleto = repo
                .boletoExterno(n.codigoExterno())
                .orElseThrow(() -> new RegraNegocioException("Cobrança desconhecida: " + n.codigoExterno()));
            if (n.situacao() != SituacaoCobranca.PAGA) return false;
            boolean quitouAgora = boleto.quitar(); // false se já estava quitado
            if (quitouAgora) repo.salvarBoleto(boleto);
            return quitouAgora;
        });
    }

    public Optional<BoletoMulta> multaDa(Devolucao d) {
        return repo.boleto(codigoDa(d));
    }
}
