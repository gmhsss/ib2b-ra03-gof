package br.edu.pucpr.ib2b.persistencia;

import br.edu.pucpr.ib2b.dominio.*;
import java.util.*;
import java.util.function.Supplier;

/** Uma única família coerente: mesma UoW e repositórios. Snapshot para rollback. */
public final class FabricaMemoria implements FabricaPersistencia {

    private Map<String, Livro> livros = new LinkedHashMap<>();
    private Map<String, Pessoa> pessoas = new LinkedHashMap<>();
    private Map<String, Estudante> estudantes = new LinkedHashMap<>();
    private Map<String, Atendente> atendentes = new LinkedHashMap<>();
    private Map<String, Reserva> reservas = new LinkedHashMap<>();
    private Map<String, Emprestimo> emprestimos = new LinkedHashMap<>();
    private Map<String, Devolucao> devolucoes = new LinkedHashMap<>();
    private Map<String, BoletoMulta> boletos = new LinkedHashMap<>();
    private final LivroRepositorio rl = new LivroRepositorio() {
        public Optional<Livro> buscar(String id) {
            return Optional.ofNullable(livros.get(id));
        }

        public void salvar(Livro l) {
            livros.put(l.id(), l);
        }

        public void remover(String id) {
            livros.remove(id);
        }

        public List<Livro> listar() {
            return List.copyOf(livros.values());
        }
    };
    private final RelacionamentoRepositorio rr = new RelacionamentoRepositorio() {
        public Optional<Pessoa> pessoa(String cpf) {
            return Optional.ofNullable(pessoas.get(cpf));
        }

        public Optional<Estudante> estudante(String id) {
            return Optional.ofNullable(estudantes.get(id));
        }

        public Optional<Atendente> atendente(String id) {
            return Optional.ofNullable(atendentes.get(id));
        }

        public void salvarPessoa(Pessoa p) {
            pessoas.put(p.cpf(), p);
        }

        public void salvarEstudante(Estudante e) {
            estudantes.put(e.id(), e);
        }

        public void salvarAtendente(Atendente a) {
            atendentes.put(a.id(), a);
        }

        public void removerPessoa(String cpf) {
            pessoas.remove(cpf);
        }

        public void removerEstudante(String id) {
            estudantes.remove(id);
        }

        public void removerAtendente(String id) {
            atendentes.remove(id);
        }

        public boolean cpfTemPapel(String cpf) {
            return (
                estudantes.values().stream().anyMatch(e -> e.cpf().equals(cpf)) ||
                atendentes.values().stream().anyMatch(a -> a.cpf().equals(cpf))
            );
        }
    };
    private final MovimentacaoRepositorio rm = new MovimentacaoRepositorio() {
        public Optional<Reserva> reserva(String id) {
            return Optional.ofNullable(reservas.get(id));
        }

        public void salvarReserva(Reserva r) {
            reservas.put(r.idLivro(), r);
        }

        public void removerReserva(String id) {
            reservas.remove(id);
        }

        public Optional<Emprestimo> emprestimo(String id) {
            return Optional.ofNullable(emprestimos.get(id));
        }

        public void salvarEmprestimo(Emprestimo e) {
            emprestimos.put(e.idLivro(), e);
        }

        public Optional<Devolucao> devolucao(String id) {
            return Optional.ofNullable(devolucoes.get(id));
        }

        public void salvarDevolucao(Devolucao d) {
            devolucoes.put(d.idLivro(), d);
        }

        public Optional<BoletoMulta> boleto(String id) {
            return Optional.ofNullable(boletos.get(id));
        }

        public Optional<BoletoMulta> boletoExterno(String ext) {
            return boletos.values().stream().filter(b -> b.codigoExterno().equals(ext)).findFirst();
        }

        public void salvarBoleto(BoletoMulta b) {
            boletos.put(b.codigo(), b);
        }

        public List<BoletoMulta> boletos() {
            return List.copyOf(boletos.values());
        }
    };
    private final UnidadeDeTrabalho uow = new UnidadeDeTrabalho() {
        public synchronized <T> T executar(Supplier<T> operacao) {
            var l = copiarLivros(livros);
            var p = new LinkedHashMap<>(pessoas);
            var e = new LinkedHashMap<>(estudantes);
            var a = new LinkedHashMap<>(atendentes);
            var r = new LinkedHashMap<>(reservas);
            var em = copiarEmprestimos(emprestimos);
            var d = new LinkedHashMap<>(devolucoes);
            var b = copiarBoletos(boletos);
            try {
                return operacao.get();
            } catch (RuntimeException | Error ex) {
                livros = l;
                pessoas = p;
                estudantes = e;
                atendentes = a;
                reservas = r;
                emprestimos = em;
                devolucoes = d;
                boletos = b;
                throw ex;
            }
        }
    };

    private static Map<String, Livro> copiarLivros(Map<String, Livro> src) {
        var out = new LinkedHashMap<String, Livro>();
        src.forEach((k, v) -> out.put(k, v.copia()));
        return out;
    }

    private static Map<String, Emprestimo> copiarEmprestimos(Map<String, Emprestimo> src) {
        var out = new LinkedHashMap<String, Emprestimo>();
        src.forEach((k, v) -> out.put(k, v.copia()));
        return out;
    }

    private static Map<String, BoletoMulta> copiarBoletos(Map<String, BoletoMulta> src) {
        var out = new LinkedHashMap<String, BoletoMulta>();
        src.forEach((k, v) -> out.put(k, v.copia()));
        return out;
    }

    public LivroRepositorio livros() {
        return rl;
    }

    public MovimentacaoRepositorio movimentacao() {
        return rm;
    }

    public RelacionamentoRepositorio relacionamento() {
        return rr;
    }

    public UnidadeDeTrabalho unidadeDeTrabalho() {
        return uow;
    }

    public String familia() {
        return "MEMORIA";
    }
}
