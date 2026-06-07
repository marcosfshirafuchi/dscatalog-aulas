package com.devsuperior.dscatalog.util;

// Entidade Product.
import com.devsuperior.dscatalog.entities.Product;

// Projeção utilizada na consulta paginada.
import com.devsuperior.dscatalog.projections.ProjectProjection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Classe utilitária que contém métodos auxiliares reutilizáveis.
 *
 * Neste caso, ela possui um método responsável por reorganizar
 * uma lista de produtos mantendo a mesma ordem da consulta paginada.
 */
public class Utils {

    /**
     * Reorganiza a lista de produtos para manter a mesma ordem
     * retornada pela consulta paginada.
     *
     * Cenário:
     *
     * 1) Primeiro fazemos uma consulta paginada que retorna apenas
     *    os IDs dos produtos (ProjectProjection).
     *
     * Exemplo:
     *
     * [3, 7, 1]
     *
     * 2) Depois fazemos uma segunda consulta utilizando JOIN FETCH
     *    para carregar os produtos completos com suas categorias.
     *
     * Porém o banco pode retornar os produtos em outra ordem:
     *
     * [1, 3, 7]
     *
     * 3) Este método reorganiza os produtos para que fiquem novamente:
     *
     * [3, 7, 1]
     *
     * Dessa forma a paginação permanece correta.
     *
     * @param ordered
     * Lista original retornada pela consulta paginada.
     * Contém a ordem correta dos produtos.
     *
     * @param unordered
     * Lista de produtos carregados com JOIN FETCH.
     * Pode estar em ordem diferente da paginação.
     *
     * @return
     * Lista de produtos reorganizada na ordem correta.
     */
    public static List<Product> replace(
            List<ProjectProjection> ordered,
            List<Product> unordered) {

        /**
         * HashMap utilizado para permitir busca rápida dos produtos.
         *
         * Estrutura:
         *
         * chave = id do produto
         * valor = objeto Product
         *
         * Exemplo:
         *
         * {
         *   1 -> Product(1),
         *   3 -> Product(3),
         *   7 -> Product(7)
         * }
         */
        Map<Long, Product> map = new HashMap<>();

        /**
         * Percorre todos os produtos carregados do banco
         * e os adiciona no mapa.
         */
        for (Product obj : unordered) {
            map.put(obj.getId(), obj);
        }

        /**
         * Lista que armazenará o resultado final
         * na ordem correta.
         */
        List<Product> result = new ArrayList<>();

        /**
         * Percorre a lista paginada original.
         *
         * Como ela possui a ordem correta dos IDs,
         * utilizamos cada ID para buscar o Product
         * correspondente dentro do HashMap.
         */
        for (ProjectProjection obj : ordered) {

            /**
             * Recupera o produto pelo ID e adiciona
             * ao resultado na posição correta.
             */
            result.add(map.get(obj.getId()));
        }

        /**
         * Retorna a lista reorganizada.
         */
        return result;
    }
}