package com.devsuperior.dscatalog.repositories;

// Importa a entidade Product, que será gerenciada por este repositório.
import com.devsuperior.dscatalog.entities.Product;

// Importa a projeção utilizada para retornar apenas os campos necessários da consulta.
import com.devsuperior.dscatalog.projections.ProductProjection;

// Classes utilizadas para paginação.
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

// Interface base do Spring Data JPA que fornece métodos CRUD prontos.
import org.springframework.data.jpa.repository.JpaRepository;

// Permite criar consultas personalizadas utilizando JPQL ou SQL nativo.
import org.springframework.data.jpa.repository.Query;

// Indica que esta interface pertence à camada de acesso a dados.
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository responsável pelas operações de acesso aos dados da entidade Product.
 *
 * Ao estender JpaRepository<Product, Long>, herdamos automaticamente diversos
 * métodos prontos, como:
 *
 * save()
 * findById()
 * findAll()
 * deleteById()
 * existsById()
 *
 * sem precisar implementá-los manualmente.
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * Consulta paginada de produtos com filtros opcionais por:
     *
     * - Nome do produto
     * - Categorias
     *
     * Esta consulta utiliza SQL nativo (nativeQuery = true).
     *
     * O retorno não é a entidade Product completa, mas sim uma projeção
     * (ProjectProjection), contendo apenas os campos necessários para a busca.
     *
     * Isso melhora a performance, pois reduz a quantidade de dados trafegados.
     *
     * Exemplo:
     *
     * name = "pc"
     * categoryIds = [1,2]
     *
     * Retornará produtos que contenham "pc" no nome e pertençam às
     * categorias 1 ou 2.
     */
    @Query(
            nativeQuery = true,
            value = """
            SELECT * FROM (
                SELECT DISTINCT tb_product.id, tb_product.name
                FROM tb_product
                INNER JOIN tb_product_category
                    ON tb_product.id = tb_product_category.product_id
                WHERE (:categoryIds IS NULL OR tb_product_category.category_id IN :categoryIds)
                AND LOWER(tb_product.name) LIKE LOWER(CONCAT('%', :name, '%'))
                ORDER BY tb_product.name
            ) AS tb_result
            """,
            countQuery = """
            SELECT COUNT(*) FROM (
                SELECT DISTINCT tb_product.id, tb_product.name
                FROM tb_product
                INNER JOIN tb_product_category
                    ON tb_product.id = tb_product_category.product_id
                WHERE (:categoryIds IS NULL OR tb_product_category.category_id IN :categoryIds)
                AND LOWER(tb_product.name) LIKE LOWER(CONCAT('%', :name, '%'))
            ) AS tb_result
            """
    )
    Page<ProductProjection> searchProducts(
            List<Long> categoryIds,
            String name,
            Pageable pageable
    );
    /**
     * Busca os produtos juntamente com suas categorias.
     *
     * O JOIN FETCH é utilizado para evitar o problema conhecido como:
     *
     * N + 1 Queries
     *
     * Sem o JOIN FETCH:
     *
     * 1 consulta busca os produtos.
     * Depois uma consulta adicional para cada produto buscar suas categorias.
     *
     * Exemplo:
     *
     * 10 produtos retornados
     * =
     * 1 consulta de produtos
     * + 10 consultas de categorias
     *
     * Total = 11 consultas
     *
     * Com JOIN FETCH:
     *
     * Apenas 1 consulta busca produtos e categorias ao mesmo tempo.
     *
     * Isso melhora significativamente a performance.
     *
     * O filtro:
     *
     * obj.id IN :productIds
     *
     * garante que apenas os produtos da página atual sejam carregados.
     */
    @Query("""
            SELECT obj
            FROM Product obj
            JOIN FETCH obj.categories
            WHERE obj.id IN :productIds ORDER BY obj.name
            """)
    List<Product> searchProductsWithCategories(List<Long> productIds);
}