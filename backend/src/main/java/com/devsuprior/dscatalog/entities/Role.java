package com.devsuprior.dscatalog.entities;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Objects;

// @Entity: Define que a classe é uma entidade JPA que será mapeada para uma tabela no banco de dados.
@Entity
// @Table: Define o nome da tabela no banco de dados (tb_role).
@Table(name = "tb_role")
public class Role implements Serializable {
    private static final long serialVersionUID = 1L;

    // @Id: Define que o campo id é a chave primária da tabela.
    @Id
    // @GeneratedValue: Define a estratégia de geração automática de IDs (Auto-incremento no banco).
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // authority: Campo que armazena a descrição da permissão ou papel do usuário (ex: ROLE_ADMIN, ROLE_OPERATOR).
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

    public String getAuthority() {
        return authority;
    }

    public void setAuthority(String authority) {
        this.authority = authority;
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof Role role)) return false;

        return Objects.equals(id, role.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}