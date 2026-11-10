package br.edu.pucpr.ib2b;

import java.lang.reflect.Proxy;
import java.sql.*;
import java.util.*;

/**
 * Driver JDBC falso (só para teste): registra quais conexões foram abertas, em qual conexão
 * cada comando SQL rodou e se houve commit/rollback. Permite testar a família MySQL sem servidor.
 */
final class JdbcFalso {

    record Comando(int conexao, String sql) {}

    final List<Comando> comandos = new ArrayList<>();
    final List<String> eventos = new ArrayList<>();
    int conexoesAbertas;

    Connection abrir() {
        int id = ++conexoesAbertas;
        return (Connection) Proxy.newProxyInstance(
            getClass().getClassLoader(),
            new Class<?>[] { Connection.class },
            (proxy, m, args) ->
                switch (m.getName()) {
                    case "prepareStatement" -> comando(id, (String) args[0], PreparedStatement.class);
                    case "createStatement" -> comando(id, null, Statement.class);
                    case "setAutoCommit" -> {
                        eventos.add("conexao" + id + ".autoCommit=" + args[0]);
                        yield null;
                    }
                    case "commit", "rollback", "close" -> {
                        eventos.add("conexao" + id + "." + m.getName());
                        yield null;
                    }
                    default -> padrao(m.getReturnType());
                }
        );
    }

    private Object comando(int conexao, String sqlPreparado, Class<?> tipo) {
        return Proxy.newProxyInstance(
            getClass().getClassLoader(),
            new Class<?>[] { tipo },
            (proxy, m, args) ->
                switch (m.getName()) {
                    case "executeUpdate" -> {
                        comandos.add(
                            new Comando(conexao, sqlPreparado != null ? sqlPreparado : (String) args[0])
                        );
                        yield 1;
                    }
                    case "executeQuery" -> {
                        comandos.add(new Comando(conexao, sqlPreparado));
                        yield Proxy.newProxyInstance(
                            getClass().getClassLoader(),
                            new Class<?>[] { ResultSet.class },
                            (p, mm, a) -> mm.getName().equals("next") ? false : padrao(mm.getReturnType())
                        );
                    }
                    default -> padrao(m.getReturnType());
                }
        );
    }

    private static Object padrao(Class<?> tipo) {
        if (tipo == boolean.class) return false;
        if (tipo == int.class) return 0;
        if (tipo == long.class) return 0L;
        return null;
    }
}
