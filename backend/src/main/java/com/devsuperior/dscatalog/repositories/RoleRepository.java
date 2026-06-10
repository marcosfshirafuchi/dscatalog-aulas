package com.devsuperior.dscatalog.repositories;

import com.devsuperior.dscatalog.entities.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository responsável pelo acesso aos dados da entidade Role.
 *
 * Uma Role representa um perfil/permissão dentro do sistema.
 *
 * Exemplos:
 *
 * ROLE_ADMIN
 * ROLE_OPERATOR
 *
 * Essas permissões são utilizadas pelo Spring Security
 * para controlar quais recursos cada usuário pode acessar.
 *
 * Exemplo:
 *
 * @PreAuthorize("hasRole('ROLE_ADMIN')")
 *
 * Apenas usuários que possuam a role ROLE_ADMIN
 * poderão acessar o endpoint.
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * Busca uma Role através do campo authority.
     *
     * O Spring Data JPA interpreta automaticamente
     * o nome do método e gera a consulta SQL correspondente.
     *
     * Método:
     *
     * findByAuthority(String authority)
     *
     * Equivale aproximadamente a:
     *
     * SELECT *
     * FROM tb_role
     * WHERE authority = ?
     *
     * Exemplo de uso:
     *
     * Role role =
     *     roleRepository.findByAuthority("ROLE_OPERATOR");
     *
     * Resultado:
     *
     * id = 2
     * authority = ROLE_OPERATOR
     *
     * Muito utilizado durante o cadastro de usuários
     * para atribuir automaticamente um perfil padrão.
     *
     * Exemplo:
     *
     * User novoUsuario = new User();
     *
     * Role role =
     *     roleRepository.findByAuthority("ROLE_OPERATOR");
     *
     * novoUsuario.getRoles().add(role);
     *
     * @param authority
     * Nome da permissão/perfil.
     *
     * Exemplos:
     * ROLE_ADMIN
     * ROLE_OPERATOR
     *
     * @return
     * Entidade Role encontrada.
     */
    Role findByAuthority(String authority);
}