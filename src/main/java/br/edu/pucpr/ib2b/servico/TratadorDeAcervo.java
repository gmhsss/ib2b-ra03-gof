package br.edu.pucpr.ib2b.servico;

import br.edu.pucpr.ib2b.dominio.*;
import br.edu.pucpr.ib2b.padroes.*;
import br.edu.pucpr.ib2b.persistencia.*;
import java.util.Optional;

public final class TratadorDeAcervo implements iLivro {

    private final CadastroLivro cadastro;
    private final Acervo acervo;
    private final ConsultaAcervo consulta;

    /** @param consulta cadeia de decoradores montada pela instalação (SistemaIB2B) */
    public TratadorDeAcervo(FabricaPersistencia f, ConsultaAcervo consulta) {
        cadastro = new CadastroLivro(f);
        acervo = new Acervo(f.livros());
        this.consulta = consulta;
    }

    public Livro inserirLivro(String id, String titulo) {
        return cadastro.incluir(id, titulo);
    }

    public Livro consultarLivro(String id) {
        return acervo.exigir(id);
    }

    public Livro atualizarLivro(String id, String novoTitulo) {
        Livro atualizado = acervo.atualizarTitulo(id, novoTitulo);
        consulta.invalidar(id); // título mudou: metadado em cache fica obsoleto
        return atualizado;
    }

    public void excluirLivro(String id) {
        acervo.excluir(id);
        consulta.invalidar(id);
    }

    public Optional<LivroInfo> consultarMetadados(String id) {
        return consulta.consultar(id);
    }
}
