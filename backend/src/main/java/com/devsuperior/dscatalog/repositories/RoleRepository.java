package com.devsuperior.dscatalog.repositories;

import com.devsuperior.dscatalog.entities.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// @Repository: Indica que esta interface é um componente de repositório do Spring,
// responsável pelo acesso a dados da entidade Role.
@Repository
// public interface RoleRepository extends JpaRepository<Role, Long>:
// Define a interface do repositório para a entidade Role.
// Estende JpaRepository, fornecendo métodos CRUD (Create, Read, Update, Delete)
// e paginação para a entidade Role, onde Role é o tipo da entidade e Long é o tipo do ID.
public interface RoleRepository extends JpaRepository<Role, Long> {
}