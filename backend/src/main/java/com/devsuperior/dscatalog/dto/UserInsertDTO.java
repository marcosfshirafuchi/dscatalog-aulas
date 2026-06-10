package com.devsuperior.dscatalog.dto;

import com.devsuperior.dscatalog.services.validation.UserInsertValid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO utilizado especificamente para o cadastro de usuários.
 *
 * Esta classe herda todos os atributos de UserDTO:
 *
 * - id
 * - firstName
 * - lastName
 * - email
 * - roles
 *
 * e adiciona o campo password, necessário apenas
 * durante o processo de criação de um usuário.
 *
 * Separar UserInsertDTO de UserDTO é uma boa prática,
 * pois permite aplicar validações específicas para
 * operações de cadastro.
 *
 * Exemplo:
 *
 * POST /users
 *
 * {
 *   "firstName": "Maria",
 *   "lastName": "Silva",
 *   "email": "maria@gmail.com",
 *   "password": "12345678"
 * }
 */
@UserInsertValid
public class UserInsertDTO extends UserDTO {

    /**
     * Identificador de versão utilizado durante
     * o processo de serialização.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Senha informada durante o cadastro.
     *
     * @NotBlank
     * Não permite:
     *
     * null
     * ""
     * "     "
     *
     * @Size(min = 8)
     * Exige no mínimo 8 caracteres.
     *
     * Exemplos válidos:
     *
     * senha123
     * abcdefgh
     * minhaSenha2025
     *
     * Exemplos inválidos:
     *
     * ""
     * 123
     * abc
     */
    @NotBlank(message = "Campo obrigatório")
    @Size(min = 8, message = "Deve ter no mínimo 8 caracteres")
    private String password;

    /**
     * Construtor padrão.
     *
     * Necessário para que o Spring Boot e o Jackson
     * consigam instanciar o objeto durante a
     * desserialização do JSON recebido pela API.
     */
    public UserInsertDTO() {
        super();
    }

    /**
     * Retorna a senha informada pelo usuário.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Define a senha do usuário.
     */
    public void setPassword(String password) {
        this.password = password;
    }
}