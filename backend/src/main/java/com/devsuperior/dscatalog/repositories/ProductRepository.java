package com.devsuperior.dscatalog.repositories; // Declaração do pacote onde a interface está localizada.

import com.devsuperior.dscatalog.entities.Product; // Importa a classe da entidade Product.
import org.springframework.data.jpa.repository.JpaRepository; // Importa a interface base do Spring Data JPA para repositórios.
import org.springframework.stereotype.Repository; // Importa a anotação @Repository.

// @Repository: Indica que esta interface é um componente de repositório do Spring,
// responsável por operações de acesso a dados para a entidade Product.
@Repository
// public interface ProductRepository extends JpaRepository<Product, Long>:
// Define a interface do repositório para a entidade Product.
// Estende JpaRepository, fornecendo métodos CRUD (Create, Read, Update, Delete)
// e paginação para a entidade Product, onde Product é o tipo da entidade e Long é o tipo do ID.
public interface ProductRepository extends JpaRepository<Product, Long> {
}