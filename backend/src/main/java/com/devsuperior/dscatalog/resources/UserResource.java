package com.devsuperior.dscatalog.resources;

import com.devsuperior.dscatalog.dto.UserDTO;
import com.devsuperior.dscatalog.dto.UserInsertDTO;
import com.devsuperior.dscatalog.dto.UserUpdateDTO;
import com.devsuperior.dscatalog.services.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

/**
 * Controller REST responsável por expor os endpoints
 * relacionados aos usuários do sistema.
 *
 * Esta classe pertence à camada Resource (Controller),
 * cuja responsabilidade é:
 *
 * - Receber requisições HTTP
 * - Validar os dados recebidos
 * - Chamar a camada Service
 * - Retornar respostas HTTP apropriadas
 *
 * Fluxo:
 *
 * Cliente
 *    ↓
 * UserResource
 *    ↓
 * UserService
 *    ↓
 * Repository
 *    ↓
 * Banco de Dados
 */
@RestController
@RequestMapping(value = "/users")
public class UserResource {

    /**
     * Serviço responsável pelas regras de negócio
     * relacionadas aos usuários.
     *
     * O Controller nunca acessa diretamente o Repository.
     *
     * A comunicação sempre ocorre através da camada Service.
     */
    @Autowired
    private UserService service;

    /**
     * Endpoint responsável por listar usuários de forma paginada.
     *
     * Somente usuários com perfil ADMIN podem acessar.
     *
     * Exemplo:
     *
     * GET /users?page=0&size=10&sort=email
     *
     * Retorna:
     *
     * Página 0
     * 10 registros por página
     * Ordenados pelo email
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping

    /**
     * Pageable é montado automaticamente pelo Spring
     * utilizando os parâmetros enviados pela URL.
     *
     * Exemplos:
     *
     * page = número da página
     * size = quantidade de registros
     * sort = campo de ordenação
     *
     * Exemplo:
     *
     * /users?page=0&size=20&sort=email
     */
    public ResponseEntity<Page<UserDTO>> findAll(Pageable pageable){
        // PARAMETROS aceitos via URL: page, size, sort.
        // Chama o serviço para buscar todos os usuários de forma paginada.
        Page<UserDTO> list = service.findAllPaged(pageable);
        // Retorna uma resposta HTTP 200 OK com a lista de usuários no corpo.
        return ResponseEntity.ok().body(list);
    }

    /**
     * Busca um usuário específico através do ID.
     *
     * Exemplo:
     *
     * GET /users/5
     *
     * Retorna os dados do usuário de ID 5.
     *
     * Caso não exista:
     *
     * HTTP 404 Not Found
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping(value = "/{id}")
    public ResponseEntity<UserDTO> findById(/**
                                                 * O valor informado na URL é capturado
                                                 * automaticamente pelo Spring.
                                                 *
                                                 * Exemplo:
                                                 *
                                                 * URL:
                                                 * /users/10
                                                 *
                                                 * Resultado:
                                                 *
                                                 * id = 10
                                                 */
                                            @PathVariable Long id){
        // Chama o serviço para buscar um usuário pelo ID.
        UserDTO dto = service.findById(id);
        // Retorna uma resposta HTTP 200 OK com o usuário encontrado no corpo.
        return ResponseEntity.ok().body(dto);
    }

    /**
     * Endpoint responsável pelo cadastro de usuários.
     *
     * Recebe os dados através do corpo da requisição.
     *
     * Exemplo JSON:
     *
     * {
     *   "firstName": "Maria",
     *   "lastName": "Silva",
     *   "email": "maria@gmail.com",
     *   "password": "123456"
     * }
     *
     * Em caso de sucesso:
     *
     * HTTP 201 Created
     */
    @PostMapping
    public ResponseEntity<UserDTO> insert(/**
                                               * @Valid ativa as validações declaradas
                                               * dentro do DTO.
                                               *
                                               * Exemplos:
                                               *
                                               * @NotBlank
                                               * @Email
                                               * @Size
                                               *
                                               * Caso alguma validação falhe,
                                               * o Spring retorna automaticamente:
                                               *
                                               * HTTP 422 Unprocessable Entity
                                               */
                                          @Valid @RequestBody UserInsertDTO dto){
        // @Valid: Ativa a validação dos campos do DTO de inserção.
        // @RequestBody: Mapeia o corpo da requisição HTTP para o objeto UserInsertDTO.
        // Chama o serviço para inserir o novo usuário.
        UserDTO newDto = service.insert(dto);
        /**
         * Monta a URI do recurso recém-criado.
         *
         * Exemplo:
         *
         * Usuário criado:
         * ID = 15
         *
         * Location:
         *
         * /users/15
         *
         * Essa URI será enviada no cabeçalho
         * Location da resposta HTTP.
         */
        URI uri = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(newDto.getId())
                .toUri();
        /**
         * Retorna:
         *
         * HTTP 201 Created
         *
         * Cabeçalho:
         *
         * Location: /users/15
         *
         * Corpo:
         *
         * Dados do usuário criado.
         */
        return ResponseEntity.created(uri).body(newDto);
    }

    /**
     * Atualiza um usuário existente.
     *
     * Exemplo:
     *
     * PUT /users/5
     *
     * O ID vem pela URL
     * e os dados atualizados
     * vêm no corpo da requisição.
     *
     * Caso o usuário não exista:
     *
     * HTTP 404 Not Found
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping(value = "/{id}")
    public ResponseEntity<UserDTO> update(/**
                                               * O valor informado na URL é capturado
                                               * automaticamente pelo Spring.
                                               *
                                               * Exemplo:
                                               *
                                               * URL:
                                               * /users/10
                                               *
                                               * Resultado:
                                               *
                                               * id = 10
                                               */
                                          @PathVariable Long id, @Valid @RequestBody UserUpdateDTO dto){
        // @PathVariable Long id: Obtém o ID do usuário da URL.
        // @Valid: Ativa a validação dos campos do DTO de atualização.
        // @RequestBody: Mapeia o corpo da requisição HTTP para o objeto UserUpdateDTO.
        // Chama o serviço para atualizar o usuário com o ID fornecido.
        UserDTO newDto = service.update(id, dto);
        // Retorna uma resposta HTTP 200 OK com o usuário atualizado no corpo.
        return ResponseEntity.ok().body(newDto);
    }

    /**
     * Remove um usuário do sistema.
     *
     * Exemplo:
     *
     * DELETE /users/5
     *
     * Caso a exclusão seja realizada
     * com sucesso:
     *
     * HTTP 204 No Content
     *
     * Nenhum conteúdo é retornado
     * no corpo da resposta.
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping(value = "/{id}")
    public ResponseEntity<UserDTO> delete(/**
                                               * O valor informado na URL é capturado
                                               * automaticamente pelo Spring.
                                               *
                                               * Exemplo:
                                               *
                                               * URL:
                                               * /users/10
                                               *
                                               * Resultado:
                                               *
                                               * id = 10
                                               */
                                          @PathVariable Long id){
        // @PathVariable Long id: Obtém o ID do usuário da URL.
        // Chama o serviço para excluir o usuário com o ID fornecido.
        service.delete(id);
        /**
         * Retorna:
         *
         * HTTP 204 No Content
         *
         * Indica que a operação foi concluída
         * com sucesso e não existe conteúdo
         * para retornar ao cliente.
         */
        return ResponseEntity.noContent().build();
    }
}