package com.devsuperior.dscatalog.repositories; // Declaração do pacote onde a interface está localizada.

import com.devsuperior.dscatalog.entities.Category; // Importa a classe da entidade Category.
import org.springframework.data.jpa.repository.JpaRepository; // Importa a interface base do Spring Data JPA para repositórios.
import org.springframework.stereotype.Repository; // Importa a anotação @Repository.

// @Repository: Indica que esta interface é um componente de repositório do Spring,
// responsável por operações de acesso a dados para a entidade Category.
@Repository
// public interface CategoryRepository extends JpaRepository<Category, Long>:
// Define a interface do repositório para a entidade Category.
// Estende JpaRepository, fornecendo métodos CRUD (Create, Read, Update, Delete)
// e paginação para a entidade Category, onde Category é o tipo da entidade e Long é o tipo do ID.
public interface CategoryRepository extends JpaRepository<Category, Long> {
}