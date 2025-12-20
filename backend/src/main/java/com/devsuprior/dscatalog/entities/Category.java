package com.devsuprior.dscatalog.entities;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Objects;

@Entity
//Define o nome da tabela do banco de dados
@Table(name = "tb_category")
public class Category implements Serializable {
    private static final long serialVersionUID = 1L;

    //Definindo o id da tabela tb_category
    @Id
    //Gera automaticamente o número do Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    public Category(){

    }

    public Category(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof Category category)) return false;

        return Objects.equals(id, category.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
