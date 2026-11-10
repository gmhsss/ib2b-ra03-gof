package br.edu.pucpr.ib2b.padroes;

import java.util.*;

/** Composite — Component: grupo e item respondem às mesmas operações. */
public interface ElementoAcervo {
    int contar();
    List<String> listarIds();
}
