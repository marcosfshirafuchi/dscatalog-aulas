package com.devsuperior.dscatalog.util;

// Importa a interface de projeção genérica.
// Qualquer objeto que implemente essa interface deve possuir um método getId().
import com.devsuperior.dscatalog.projections.IdProjection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Classe utilitária que contém métodos auxiliares reutilizáveis.
 *
 * O objetivo desta classe é fornecer funcionalidades genéricas
 * que podem ser utilizadas em diferentes partes da aplicação.
 */
public class Utils {

    /**
     * Reorganiza uma lista de objetos mantendo a mesma ordem
     * retornada por uma consulta paginada.
     *
     * Essa técnica é muito utilizada quando:
     *
     * 1) Realizamos uma consulta paginada retornando apenas IDs.
     *
     * 2) Em seguida fazemos uma segunda consulta utilizando
     *    JOIN FETCH para carregar os relacionamentos.
     *
     * 3) O banco de dados pode retornar os registros em uma
     *    ordem diferente da consulta original.
     *
     * 4) Este método reorganiza os objetos para preservar
     *    a ordem correta da paginação.
     *
     * Exemplo:
     *
     * Lista original (ordered):
     *
     * [3, 7, 1]
     *
     * Lista carregada com JOIN FETCH (unordered):
     *
     * [1, 3, 7]
     *
     * Resultado:
     *
     * [3, 7, 1]
     *
     * @param <ID>
     * Tipo do identificador da entidade.
     *
     * Exemplos:
     * Long
     * Integer
     * UUID
     * String
     *
     * @param ordered
     * Lista contendo os objetos na ordem correta.
     *
     * @param unordered
     * Lista contendo os mesmos objetos, porém possivelmente
     * em ordem diferente.
     *
     * @return
     * Lista reorganizada mantendo a ordem da lista original.
     */
    public static <ID> List<? extends IdProjection<ID>> replace(
            List<? extends IdProjection<ID>> ordered,
            List<? extends IdProjection<ID>> unordered) {

        /**
         * HashMap utilizado para realizar buscas rápidas.
         *
         * Estrutura:
         *
         * chave -> ID do objeto
         * valor -> objeto correspondente
         *
         * Exemplo:
         *
         * {
         *   1 -> Produto A,
         *   2 -> Produto B,
         *   3 -> Produto C
         * }
         *
         * A vantagem do HashMap é que a busca ocorre
         * praticamente em tempo constante O(1).
         */
        Map<ID, IdProjection<ID>> map = new HashMap<>();

        /**
         * Percorre a lista desordenada.
         *
         * Cada objeto é armazenado no HashMap
         * utilizando seu ID como chave.
         */
        for (IdProjection<ID> obj : unordered) {
            map.put(obj.getId(), obj);
        }

        /**
         * Lista que armazenará os objetos
         * na ordem correta.
         */
        List<IdProjection<ID>> result = new ArrayList<>();

        /**
         * Percorre a lista original ordenada.
         *
         * Como ela contém a sequência correta dos IDs,
         * utilizamos cada ID para localizar o objeto
         * correspondente dentro do HashMap.
         */
        for (IdProjection<ID> obj : ordered) {

            /**
             * Busca o objeto pelo ID.
             *
             * Exemplo:
             *
             * ordered:
             * [3, 7, 1]
             *
             * map:
             * {
             *   1 -> Produto A
             *   3 -> Produto B
             *   7 -> Produto C
             * }
             *
             * Resultado:
             * [Produto B, Produto C, Produto A]
             */
            result.add(map.get(obj.getId()));
        }

        /**
         * Retorna a lista reorganizada.
         */
        return result;
    }
}