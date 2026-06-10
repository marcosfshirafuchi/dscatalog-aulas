package com.devsuperior.dscatalog.dto;

import com.devsuperior.dscatalog.entities.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * DTO (Data Transfer Object) utilizado para transportar
 * dados de usuários entre as camadas da aplicação.
 *
 * O objetivo de um DTO é evitar que a entidade User
 * seja exposta diretamente para a camada de apresentação
 * (Controller/API).
 *
 * Vantagens:
 *
 * - Maior segurança
 * - Menor acoplamento
 * - Controle dos dados enviados ao cliente
 * - Facilita validações
 *
 * Fluxo:
 *
 * Entidade User
 *        ↓
 *      UserDTO
 *        ↓
 *      JSON
 *        ↓
 *      Cliente
 */
public class UserDTO implements Serializable {

    /**
     * Identificador de versão utilizado pela serialização Java.
     *
     * Ajuda a garantir compatibilidade entre diferentes versões
     * da classe durante processos de serialização e desserialização.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Identificador único do usuário.
     */
    private Long id;

    /**
     * Nome do usuário.
     *
     * @NotBlank:
     * Não permite valor nulo, vazio ou contendo apenas espaços.
     *
     * Exemplo válido:
     * João
     *
     * Exemplo inválido:
     * ""
     */
    @NotBlank(message = "Campo obrigatório")
    private String firstName;

    /**
     * Sobrenome do usuário.
     */
    private String lastName;

    /**
     * E-mail do usuário.
     *
     * @Email:
     * Valida automaticamente o formato do e-mail.
     *
     * Exemplo válido:
     * joao@gmail.com
     *
     * Exemplo inválido:
     * joao.gmail.com
     */
    @Email(message = "Favor entrar com e-mail válido")
    private String email;

    /**
     * Perfis (roles) associados ao usuário.
     *
     * Exemplo:
     *
     * ROLE_ADMIN
     * ROLE_OPERATOR
     *
     * Utilizamos Set para evitar perfis duplicados.
     */
    Set<RoleDTO> roles = new HashSet<>();

    /**
     * Construtor padrão.
     *
     * Necessário para frameworks como:
     *
     * - Spring Boot
     * - Jackson
     * - Hibernate
     *
     * durante processos de serialização e desserialização.
     */
    public UserDTO() {

    }

    /**
     * Construtor utilizado para criar um DTO manualmente.
     *
     * Exemplo:
     *
     * UserDTO dto =
     *      new UserDTO(
     *          1L,
     *          "Maria",
     *          "Silva",
     *          "maria@gmail.com"
     *      );
     */
    public UserDTO(Long id, String firstName, String lastName, String email) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }

    /**
     * Construtor responsável por converter uma entidade User
     * para um UserDTO.
     *
     * Esse processo é chamado de:
     *
     * Entity → DTO Mapping
     *
     * Além dos dados básicos do usuário,
     * também converte todos os perfis (roles)
     * associados ao usuário.
     */
    public UserDTO(User entity) {

        // Copia os atributos básicos da entidade.
        id = entity.getId();
        firstName = entity.getFirstName();
        lastName = entity.getLastName();
        email = entity.getEmail();

        /**
         * Converte cada Role da entidade
         * em um RoleDTO.
         *
         * Exemplo:
         *
         * User
         *   ├── ROLE_ADMIN
         *   └── ROLE_OPERATOR
         *
         * Resultado:
         *
         * UserDTO
         *   ├── RoleDTO(ROLE_ADMIN)
         *   └── RoleDTO(ROLE_OPERATOR)
         */
        entity.getRoles().forEach(
                role -> this.roles.add(new RoleDTO(role))
        );
    }

    /**
     * Retorna o ID do usuário.
     */
    public Long getId() {
        return id;
    }

    /**
     * Define o ID do usuário.
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Retorna o nome do usuário.
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Define o nome do usuário.
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Retorna o sobrenome do usuário.
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Define o sobrenome do usuário.
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Retorna o e-mail do usuário.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Define o e-mail do usuário.
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Retorna a coleção de perfis do usuário.
     *
     * Exemplo:
     *
     * [
     *   ROLE_ADMIN,
     *   ROLE_OPERATOR
     * ]
     */
    public Set<RoleDTO> getRoles() {
        return roles;
    }
}