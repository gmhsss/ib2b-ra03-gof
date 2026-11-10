package br.edu.pucpr.ib2b.padroes;

import java.util.*;
import java.util.concurrent.*;

/**
 * Decorator — cache de METADADOS (id, título). Nunca guarda estado nem disponibilidade,
 * por isso não participa da decisão de reserva (a fachada lê o Livro atual do repositório).
 * Se o armazenamento do cache falhar, a consulta segue para o componente interno (degrada, não quebra).
 */
public final class ConsultaComCache extends DecoradorConsulta {

    private final Map<String, LivroInfo> cache;

    public ConsultaComCache(ConsultaAcervo interno) {
        this(interno, new ConcurrentHashMap<>());
    }

    /** Permite trocar o armazenamento (ex.: um cache externo) — usado no teste de falha. */
    public ConsultaComCache(ConsultaAcervo interno, Map<String, LivroInfo> armazenamento) {
        super(interno);
        this.cache = Objects.requireNonNull(armazenamento);
    }

    @Override
    public Optional<LivroInfo> consultar(String id) {
        LivroInfo emCache = lerDoCache(id);
        if (emCache != null) return Optional.of(emCache); // hit: não chama o interno
        Optional<LivroInfo> resultado = interno.consultar(id); // miss: delega
        resultado.ifPresent(info -> gravarNoCache(id, info)); // "não encontrado" não é cacheado
        return resultado;
    }

    @Override
    public void invalidar(String id) {
        try {
            cache.remove(id);
        } catch (RuntimeException ignorada) {
            // cache indisponível: nada a invalidar
        }
        super.invalidar(id); // outros caches mais internos também são avisados
    }

    private LivroInfo lerDoCache(String id) {
        try {
            return cache.get(id);
        } catch (RuntimeException falhaDoCache) {
            return null; // cache fora do ar => tratado como miss
        }
    }

    private void gravarNoCache(String id, LivroInfo info) {
        try {
            cache.put(id, info);
        } catch (RuntimeException falhaDoCache) {
            // ignora: o resultado correto já veio do componente interno
        }
    }
}
