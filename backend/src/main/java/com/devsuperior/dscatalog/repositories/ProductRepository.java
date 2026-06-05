package com.devsuperior.dscatalog.repositories; // Declaração do pacote onde a interface está localizada.

import com.devsuperior.dscatalog.entities.Product; // Importa a classe da entidade Product.
import com.devsuperior.dscatalog.projections.ProjectProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository; // Importa a interface base do Spring Data JPA para repositórios.
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository; // Importa a anotação @Repository.

import org.springframework.data.domain.Pageable; // <-- CORRIGIDO: Importação correta do Pageable
import java.util.List;

// @Repository: Indica que esta interface é um componente de repositório do Spring,
// responsável por operações de acesso a dados para a entidade Product.
@Repository
// public interface ProductRepository extends JpaRepository<Product, Long>:
// Define a interface do repositório para a entidade Product.
// Estende JpaRepository, fornecendo métodos CRUD (Create, Read, Update, Delete)
// e paginação para a entidade Product, onde Product é o tipo da entidade e Long é o tipo do ID.
public interface ProductRepository extends JpaRepository<Product, Long> {
    @Query(nativeQuery = true, value = """
            SELECT DISTINCT tb_product.id, tb_product.name
            FROM tb_product
            INNER JOIN tb_product_category ON tb_product.id = tb_product_category.product_id
            WHERE (:categoryIds IS NULL OR tb_product_category.category_id IN :categoryIds)
            AND LOWER(tb_product.name) LIKE  lower(CONCAT('%',:name,'%'))
            ORDER BY tb_product.name
            """, countQuery = """
            SELECT COUNT(*) FROM(
            SELECT DISTINCT tb_product.id, tb_product.name
            FROM tb_product
            INNER JOIN tb_product_category ON tb_product.id = tb_product_category.product_id
            WHERE (:categoryIds IS NULL OR tb_product_category.category_id IN :categoryIds)
            AND LOWER(tb_product.name) LIKE  lower(CONCAT('%',:name,'%'))
            ORDER BY tb_product.name
            ) AS tb_result
            """)
    Page<ProjectProjection> searchProducts(List<Long> categoryIds, String name, Pageable pageable);
}