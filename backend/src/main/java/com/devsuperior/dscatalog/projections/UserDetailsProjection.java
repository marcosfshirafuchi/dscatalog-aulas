package com.devsuperior.dscatalog.projections;

// UserDetailsProjection é uma interface de projeção (ou DTO de projeção) usada com Spring Data JPA.
// Ela permite que o repositório retorne apenas um subconjunto dos dados de uma entidade,
// otimizando consultas ao buscar informações específicas do usuário e seus papéis (roles)
// para o Spring Security.
public interface UserDetailsProjection {
    // getUsername(): Retorna o nome de usuário (geralmente o email) do usuário.
    String getUsername();
    // getPassword(): Retorna a senha codificada do usuário.
    String getPassword();
    // getRoleId(): Retorna o ID de um papel (role) associado ao usuário.
    Long getRoleId();
    // getAuthority(): Retorna a descrição da autoridade (ex: "ROLE_ADMIN") de um papel associado ao usuário.
    String getAuthority();
}