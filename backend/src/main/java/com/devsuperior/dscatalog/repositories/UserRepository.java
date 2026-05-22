package com.devsuperior.dscatalog.repositories;

import com.devsuperior.dscatalog.entities.User;
import com.devsuperior.dscatalog.projections.UserDetailsProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

// @Repository: Indica que esta interface é um componente de repositório do Spring,
// responsável por operações de acesso a dados para a entidade User.
// Estende JpaRepository: Fornece métodos CRUD (Create, Read, Update, Delete)
// e funcionalidades de paginação e ordenação para a entidade User (Long é o tipo da chave primária).
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

	// findByEmail: Método de consulta derivado do Spring Data JPA.
	// O Spring Data JPA gera automaticamente a implementação da consulta
	// para buscar um User pelo seu campo 'email'.
	User findByEmail(String email);

	// @Query: Define uma consulta SQL nativa personalizada.
	// nativeQuery = true: Indica que a consulta é SQL puro, não JPQL.
	// O valor da query é uma String multi-linha (text block) para melhor legibilidade.
	@Query(nativeQuery = true, value = """
			SELECT tb_user.email AS username, tb_user.password, tb_role.id AS roleId, tb_role.authority
			FROM tb_user
			INNER JOIN tb_user_role ON tb_user.id = tb_user_role.user_id
			INNER JOIN tb_role ON tb_role.id = tb_user_role.role_id
			WHERE tb_user.email = :email
		""")
	// searchUserAndRolesByEmail: Este método executa a consulta SQL nativa definida acima.
	// Ele busca o email, senha e os roles (id e authority) de um usuário específico.
	// O resultado da consulta é mapeado para uma lista de objetos UserDetailsProjection,
	// que é uma interface que define os campos que serão retornados pela consulta.
	// O parâmetro ':email' na query é preenchido com o valor do argumento 'email' do método.
	List<UserDetailsProjection> searchUserAndRolesByEmail(String email);
}