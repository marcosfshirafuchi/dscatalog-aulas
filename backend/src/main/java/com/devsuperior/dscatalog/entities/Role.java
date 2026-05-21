package com.devsuperior.dscatalog.entities;

import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;

import java.util.Objects;

// @Entity: Define que a classe é uma entidade JPA que será mapeada para uma tabela no banco de dados.
@Entity
// @Table: Define o nome da tabela no banco de dados (tb_role).
@Table(name = "tb_role")
// A classe Role implementa GrantedAuthority, uma interface do Spring Security que representa uma permissão
// concedida a um principal (usuário). É usada para definir os perfis de acesso no sistema.
public class Role implements GrantedAuthority {
    private static final long serialVersionUID = 1L;

    // @Id: Define que o campo id é a chave primária da tabela.
    @Id
    // @GeneratedValue: Define a estratégia de geração automática de IDs (Auto-incremento no banco).
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // authority: Campo que armazena a descrição da permissão ou papel do usuário (ex: ROLE_ADMIN, ROLE_OPERATOR).
    // Este campo é o que o Spring Security usa para identificar a autoridade.
    private String authority;

    public Role(){

    }

    public Role(Long id, String authority) {
        this.id = id;
        this.authority = authority;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    // getAuthority(): Método da interface GrantedAuthority que retorna a string que representa a autoridade.
    // Por exemplo, "ROLE_ADMIN" ou "ROLE_OPERATOR".
    @Override
    public String getAuthority() {
        return authority;
    }

    public void setAuthority(String authority) {
        this.authority = authority;
    }

    // equals(): Sobrescrita do método equals para comparar objetos Role com base no seu ID.
    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof Role role)) return false;

        return Objects.equals(id, role.id);
    }

    // hashCode(): Sobrescrita do método hashCode para gerar um código hash baseado no ID do Role.
    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}