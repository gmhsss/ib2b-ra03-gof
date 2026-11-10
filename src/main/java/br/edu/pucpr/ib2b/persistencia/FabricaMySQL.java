package br.edu.pucpr.ib2b.persistencia;

import br.edu.pucpr.ib2b.dominio.*;
import java.sql.*;
import java.time.*;
import java.util.*;
import java.util.function.Supplier;

/**
 * Família MySQL com tabelas relacionais de domínio normalizadas.
 * Todos os repositórios compartilham a Connection de uma UnidadeDeTrabalho;
 * fora de uma transação, cada chamada abre e fecha a própria conexão.
 * Não existe escrita no Oracle (réplica do modelo arquitetural anterior).
 */
public final class FabricaMySQL implements FabricaPersistencia {

    /** De onde vêm as conexões JDBC (DriverManager em produção; substituível em teste). */
    @FunctionalInterface
    public interface FonteDeConexoes {
        Connection abrir() throws SQLException;
    }

    private final FonteDeConexoes fonte;
    /** Conexão da transação em curso nesta thread: é ela que os TRÊS repositórios usam. */
    private final ThreadLocal<Connection> atual = new ThreadLocal<>();

    @FunctionalInterface
    private interface AcessoSQL<T> {
        T executar(Connection c) throws SQLException;
    }

    @FunctionalInterface
    private interface Parametros {
        void preencher(PreparedStatement p) throws SQLException;
    }

    @FunctionalInterface
    private interface Mapeamento<T> {
        T ler(ResultSet r) throws SQLException;
    }

    public FabricaMySQL(String url, String usuario, String senha) {
        this(() -> DriverManager.getConnection(url, usuario, senha));
    }

    public FabricaMySQL(FonteDeConexoes fonte) {
        this.fonte = Objects.requireNonNull(fonte);
        criarEsquema();
    }

    private Connection novaConexao() throws SQLException {
        return fonte.abrir();
    }

    private <T> T executarSQL(AcessoSQL<T> trabalho) {
        Connection transacao = atual.get();
        if (transacao != null) {
            try {
                return trabalho.executar(transacao);
            } catch (SQLException ex) {
                throw new IllegalStateException("Falha SQL em transação", ex);
            }
        }
        try (Connection conexao = novaConexao()) {
            return trabalho.executar(conexao);
        } catch (SQLException ex) {
            throw new IllegalStateException("Falha de persistência MySQL", ex);
        }
    }

    private <T> Optional<T> um(String sql, Parametros parametros, Mapeamento<T> mapa) {
        return executarSQL(c -> {
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                parametros.preencher(ps);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() ? Optional.of(mapa.ler(rs)) : Optional.empty();
                }
            }
        });
    }

    private <T> List<T> muitos(String sql, Parametros parametros, Mapeamento<T> mapa) {
        return executarSQL(c -> {
            List<T> saida = new ArrayList<>();
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                parametros.preencher(ps);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) saida.add(mapa.ler(rs));
                }
            }
            return List.copyOf(saida);
        });
    }

    private void atualizar(String sql, Parametros parametros) {
        executarSQL(c -> {
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                parametros.preencher(ps);
                ps.executeUpdate();
                return null;
            }
        });
    }

    private static LocalDate data(ResultSet rs, String coluna) throws SQLException {
        return rs.getDate(coluna).toLocalDate();
    }

    private static Livro mapearLivro(ResultSet rs) throws SQLException {
        return new Livro(
            rs.getString("id"),
            rs.getString("titulo"),
            EstadoLivro.valueOf(rs.getString("estado"))
        );
    }

    private static Emprestimo mapearEmprestimo(ResultSet rs) throws SQLException {
        return new Emprestimo(
            rs.getString("id_livro"),
            rs.getString("id_estudante"),
            data(rs, "inicio"),
            data(rs, "vencimento"),
            rs.getInt("renovacoes"),
            rs.getBoolean("encerrado")
        );
    }

    private static BoletoMulta mapearBoleto(ResultSet rs) throws SQLException {
        return new BoletoMulta(
            rs.getString("codigo"),
            rs.getString("id_livro"),
            rs.getString("id_estudante"),
            rs.getString("politica"),
            rs.getBigDecimal("valor"),
            data(rs, "emissao"),
            rs.getString("codigo_externo"),
            rs.getBoolean("quitado")
        );
    }

    private final LivroRepositorio livros = new LivroRepositorio() {
        public Optional<Livro> buscar(String id) {
            return um(
                "SELECT id,titulo,estado FROM livros WHERE id=?",
                ps -> ps.setString(1, id),
                FabricaMySQL::mapearLivro
            );
        }

        public void salvar(Livro l) {
            atualizar(
                "INSERT INTO livros (id,titulo,estado) VALUES (?,?,?) ON DUPLICATE KEY UPDATE titulo=VALUES(titulo),estado=VALUES(estado)",
                ps -> {
                    ps.setString(1, l.id());
                    ps.setString(2, l.titulo());
                    ps.setString(3, l.estado().name());
                }
            );
        }

        public void remover(String id) {
            atualizar("DELETE FROM livros WHERE id=?", ps -> ps.setString(1, id));
        }

        public List<Livro> listar() {
            return muitos(
                "SELECT id,titulo,estado FROM livros ORDER BY id",
                ps -> {},
                FabricaMySQL::mapearLivro
            );
        }
    };
    private final RelacionamentoRepositorio relacionamento = new RelacionamentoRepositorio() {
        public Optional<Pessoa> pessoa(String cpf) {
            return um(
                "SELECT cpf,nome,sobrenome FROM pessoas WHERE cpf=?",
                ps -> ps.setString(1, cpf),
                rs -> new Pessoa(rs.getString("cpf"), rs.getString("nome"), rs.getString("sobrenome"))
            );
        }

        public Optional<Estudante> estudante(String id) {
            return um(
                "SELECT id,cpf FROM estudantes WHERE id=?",
                ps -> ps.setString(1, id),
                rs -> new Estudante(rs.getString("id"), rs.getString("cpf"))
            );
        }

        public Optional<Atendente> atendente(String id) {
            return um(
                "SELECT id,cpf FROM atendentes WHERE id=?",
                ps -> ps.setString(1, id),
                rs -> new Atendente(rs.getString("id"), rs.getString("cpf"))
            );
        }

        public void salvarPessoa(Pessoa p) {
            atualizar(
                "INSERT INTO pessoas (cpf,nome,sobrenome) VALUES (?,?,?) ON DUPLICATE KEY UPDATE nome=VALUES(nome),sobrenome=VALUES(sobrenome)",
                ps -> {
                    ps.setString(1, p.cpf());
                    ps.setString(2, p.nome());
                    ps.setString(3, p.sobrenome());
                }
            );
        }

        public void salvarEstudante(Estudante e) {
            atualizar(
                "INSERT INTO estudantes (id,cpf) VALUES (?,?) ON DUPLICATE KEY UPDATE cpf=VALUES(cpf)",
                ps -> {
                    ps.setString(1, e.id());
                    ps.setString(2, e.cpf());
                }
            );
        }

        public void removerPessoa(String cpf) {
            atualizar("DELETE FROM pessoas WHERE cpf=?", ps -> ps.setString(1, cpf));
        }

        public void removerEstudante(String id) {
            atualizar("DELETE FROM estudantes WHERE id=?", ps -> ps.setString(1, id));
        }

        public void removerAtendente(String id) {
            atualizar("DELETE FROM atendentes WHERE id=?", ps -> ps.setString(1, id));
        }

        public boolean cpfTemPapel(String cpf) {
            return um(
                "SELECT 1 AS ligado FROM DUAL WHERE EXISTS(SELECT 1 FROM estudantes WHERE cpf=?) OR EXISTS(SELECT 1 FROM atendentes WHERE cpf=?)",
                ps -> {
                    ps.setString(1, cpf);
                    ps.setString(2, cpf);
                },
                rs -> true
            ).orElse(false);
        }

        public void salvarAtendente(Atendente a) {
            atualizar(
                "INSERT INTO atendentes (id,cpf) VALUES (?,?) ON DUPLICATE KEY UPDATE cpf=VALUES(cpf)",
                ps -> {
                    ps.setString(1, a.id());
                    ps.setString(2, a.cpf());
                }
            );
        }
    };
    private final MovimentacaoRepositorio movimentacao = new MovimentacaoRepositorio() {
        public Optional<Reserva> reserva(String idLivro) {
            return um(
                "SELECT id_livro,id_estudante,criada FROM reservas WHERE id_livro=?",
                ps -> ps.setString(1, idLivro),
                rs -> new Reserva(rs.getString("id_livro"), rs.getString("id_estudante"), data(rs, "criada"))
            );
        }

        public void salvarReserva(Reserva r) {
            atualizar(
                "INSERT INTO reservas (id_livro,id_estudante,criada) VALUES (?,?,?) ON DUPLICATE KEY UPDATE id_estudante=VALUES(id_estudante),criada=VALUES(criada)",
                ps -> {
                    ps.setString(1, r.idLivro());
                    ps.setString(2, r.idEstudante());
                    ps.setDate(3, java.sql.Date.valueOf(r.criadaEm()));
                }
            );
        }

        public void removerReserva(String idLivro) {
            atualizar("DELETE FROM reservas WHERE id_livro=?", ps -> ps.setString(1, idLivro));
        }

        public Optional<Emprestimo> emprestimo(String idLivro) {
            return um(
                "SELECT * FROM emprestimos WHERE id_livro=?",
                ps -> ps.setString(1, idLivro),
                FabricaMySQL::mapearEmprestimo
            );
        }

        public void salvarEmprestimo(Emprestimo e) {
            atualizar(
                "INSERT INTO emprestimos (id_livro,id_estudante,inicio,vencimento,renovacoes,encerrado) VALUES (?,?,?,?,?,?) ON DUPLICATE KEY UPDATE id_estudante=VALUES(id_estudante),inicio=VALUES(inicio),vencimento=VALUES(vencimento),renovacoes=VALUES(renovacoes),encerrado=VALUES(encerrado)",
                ps -> {
                    ps.setString(1, e.idLivro());
                    ps.setString(2, e.idEstudante());
                    ps.setDate(3, java.sql.Date.valueOf(e.dataInicio()));
                    ps.setDate(4, java.sql.Date.valueOf(e.vencimento()));
                    ps.setInt(5, e.numRenovacoes());
                    ps.setBoolean(6, e.encerrado());
                }
            );
        }

        public Optional<Devolucao> devolucao(String idLivro) {
            return um(
                "SELECT * FROM devolucoes WHERE id_livro=?",
                ps -> ps.setString(1, idLivro),
                rs ->
                    new Devolucao(
                        rs.getString("id_livro"),
                        rs.getString("id_estudante"),
                        data(rs, "vencimento"),
                        data(rs, "devolvida")
                    )
            );
        }

        public void salvarDevolucao(Devolucao d) {
            atualizar(
                "INSERT INTO devolucoes (id_livro,id_estudante,vencimento,devolvida) VALUES (?,?,?,?) ON DUPLICATE KEY UPDATE id_estudante=VALUES(id_estudante),vencimento=VALUES(vencimento),devolvida=VALUES(devolvida)",
                ps -> {
                    ps.setString(1, d.idLivro());
                    ps.setString(2, d.idEstudante());
                    ps.setDate(3, java.sql.Date.valueOf(d.vencimento()));
                    ps.setDate(4, java.sql.Date.valueOf(d.dataDevolucao()));
                }
            );
        }

        public Optional<BoletoMulta> boleto(String codigo) {
            return um(
                "SELECT * FROM boletos WHERE codigo=?",
                ps -> ps.setString(1, codigo),
                FabricaMySQL::mapearBoleto
            );
        }

        public Optional<BoletoMulta> boletoExterno(String idExterno) {
            return um(
                "SELECT * FROM boletos WHERE codigo_externo=? FOR UPDATE",
                ps -> ps.setString(1, idExterno),
                FabricaMySQL::mapearBoleto
            );
        }

        public void salvarBoleto(BoletoMulta b) {
            atualizar(
                "INSERT INTO boletos (codigo,id_livro,id_estudante,politica,valor,emissao,codigo_externo,quitado) VALUES (?,?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE quitado=VALUES(quitado)",
                ps -> {
                    ps.setString(1, b.codigo());
                    ps.setString(2, b.idLivro());
                    ps.setString(3, b.idEstudante());
                    ps.setString(4, b.politica());
                    ps.setBigDecimal(5, b.valor());
                    ps.setDate(6, java.sql.Date.valueOf(b.emitidoEm()));
                    ps.setString(7, b.codigoExterno());
                    ps.setBoolean(8, b.quitado());
                }
            );
        }

        public List<BoletoMulta> boletos() {
            return muitos("SELECT * FROM boletos ORDER BY codigo", ps -> {}, FabricaMySQL::mapearBoleto);
        }
    };
    private final UnidadeDeTrabalho unidadeDeTrabalho = new UnidadeDeTrabalho() {
        public <T> T executar(Supplier<T> operacao) {
            if (atual.get() != null) return operacao.get();
            try (Connection conexao = novaConexao()) {
                conexao.setAutoCommit(false);
                atual.set(conexao);
                try {
                    T resultado = operacao.get();
                    conexao.commit();
                    return resultado;
                } catch (RuntimeException | Error ex) {
                    try {
                        conexao.rollback();
                    } catch (SQLException erroRollback) {
                        ex.addSuppressed(erroRollback);
                    }
                    throw ex;
                } finally {
                    atual.remove();
                }
            } catch (SQLException ex) {
                throw new IllegalStateException("Falha na transação MySQL", ex);
            }
        }
    };

    private void criarEsquema() {
        String[] ddl = {
            "CREATE TABLE IF NOT EXISTS livros (id VARCHAR(128) PRIMARY KEY, titulo VARCHAR(255) NOT NULL, estado VARCHAR(20) NOT NULL) ENGINE=InnoDB",
            "CREATE TABLE IF NOT EXISTS pessoas (cpf CHAR(11) PRIMARY KEY, nome VARCHAR(120) NOT NULL, sobrenome VARCHAR(120) NOT NULL) ENGINE=InnoDB",
            "CREATE TABLE IF NOT EXISTS estudantes (id VARCHAR(128) PRIMARY KEY, cpf CHAR(11) NOT NULL, FOREIGN KEY (cpf) REFERENCES pessoas(cpf)) ENGINE=InnoDB",
            "CREATE TABLE IF NOT EXISTS atendentes (id VARCHAR(128) PRIMARY KEY, cpf CHAR(11) NOT NULL, FOREIGN KEY (cpf) REFERENCES pessoas(cpf)) ENGINE=InnoDB",
            "CREATE TABLE IF NOT EXISTS reservas (id_livro VARCHAR(128) PRIMARY KEY, id_estudante VARCHAR(128) NOT NULL, criada DATE NOT NULL, FOREIGN KEY (id_livro) REFERENCES livros(id), FOREIGN KEY (id_estudante) REFERENCES estudantes(id)) ENGINE=InnoDB",
            "CREATE TABLE IF NOT EXISTS emprestimos (id_livro VARCHAR(128) PRIMARY KEY, id_estudante VARCHAR(128) NOT NULL, inicio DATE NOT NULL, vencimento DATE NOT NULL, renovacoes INT NOT NULL, encerrado BOOLEAN NOT NULL, FOREIGN KEY (id_livro) REFERENCES livros(id), FOREIGN KEY (id_estudante) REFERENCES estudantes(id)) ENGINE=InnoDB",
            "CREATE TABLE IF NOT EXISTS devolucoes (id_livro VARCHAR(128) PRIMARY KEY, id_estudante VARCHAR(128) NOT NULL, vencimento DATE NOT NULL, devolvida DATE NOT NULL, FOREIGN KEY (id_livro) REFERENCES livros(id), FOREIGN KEY (id_estudante) REFERENCES estudantes(id)) ENGINE=InnoDB",
            "CREATE TABLE IF NOT EXISTS boletos (codigo VARCHAR(128) PRIMARY KEY, id_livro VARCHAR(128) NOT NULL, id_estudante VARCHAR(128) NOT NULL, politica VARCHAR(40) NOT NULL, valor DECIMAL(12,2) NOT NULL, emissao DATE NOT NULL, codigo_externo VARCHAR(200) UNIQUE NOT NULL, quitado BOOLEAN NOT NULL, FOREIGN KEY (id_livro) REFERENCES livros(id), FOREIGN KEY (id_estudante) REFERENCES estudantes(id)) ENGINE=InnoDB",
        };
        try (Connection c = novaConexao(); Statement st = c.createStatement()) {
            for (String comando : ddl) st.executeUpdate(comando);
        } catch (SQLException ex) {
            throw new IllegalStateException("Erro ao criar esquema MySQL", ex);
        }
    }

    @Override
    public LivroRepositorio livros() {
        return livros;
    }

    @Override
    public RelacionamentoRepositorio relacionamento() {
        return relacionamento;
    }

    @Override
    public MovimentacaoRepositorio movimentacao() {
        return movimentacao;
    }

    @Override
    public UnidadeDeTrabalho unidadeDeTrabalho() {
        return unidadeDeTrabalho;
    }

    @Override
    public String familia() {
        return "MYSQL";
    }
}
