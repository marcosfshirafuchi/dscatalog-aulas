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

// @RestController: Define que a classe é um controlador REST que gerencia requisições da API.
@RestController
// @RequestMapping: Define a rota base para este controlador (/users).
@RequestMapping(value = "/users")
public class UserResource {

    // @Autowired: Realiza a injeção de dependência do UserService.
    @Autowired
    private UserService service;

    // @PreAuthorize("hasRole('ROLE_ADMIN')"): Garante que apenas usuários com o papel 'ROLE_ADMIN' podem acessar este método.
    // Busca todos os usuários de forma paginada.
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping
    public ResponseEntity<Page<UserDTO>> findAll(Pageable pageable){
        // PARAMETROS aceitos via URL: page, size, sort.
        // Chama o serviço para buscar todos os usuários de forma paginada.
        Page<UserDTO> list = service.findAllPaged(pageable);
        // Retorna uma resposta HTTP 200 OK com a lista de usuários no corpo.
        return ResponseEntity.ok().body(list);
    }

    // @PreAuthorize("hasRole('ROLE_ADMIN')"): Garante que apenas usuários com o papel 'ROLE_ADMIN' podem acessar este método.
    // Busca um usuário pelo ID.
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping(value = "/{id}")
    public ResponseEntity<UserDTO> findById(@PathVariable Long id){
        // Chama o serviço para buscar um usuário pelo ID.
        UserDTO dto = service.findById(id);
        // Retorna uma resposta HTTP 200 OK com o usuário encontrado no corpo.
        return ResponseEntity.ok().body(dto);
    }

    // @PreAuthorize("hasRole('ROLE_ADMIN')"): Garante que apenas usuários com o papel 'ROLE_ADMIN' podem acessar este método.
    // Insere um novo usuário.
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping
    public ResponseEntity<UserDTO> insert(@Valid @RequestBody UserInsertDTO dto){
        // @Valid: Ativa a validação dos campos do DTO de inserção.
        // @RequestBody: Mapeia o corpo da requisição HTTP para o objeto UserInsertDTO.
        // Chama o serviço para inserir o novo usuário.
        UserDTO newDto = service.insert(dto);
        // Gera a URI do novo recurso criado no cabeçalho Location da resposta.
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(newDto.getId()).toUri();
        // Retorna uma resposta HTTP 201 Created com a URI do novo recurso e o usuário criado no corpo.
        return ResponseEntity.created(uri).body(newDto);
    }

    // @PreAuthorize("hasRole('ROLE_ADMIN')"): Garante que apenas usuários com o papel 'ROLE_ADMIN' podem acessar este método.
    // Atualiza um usuário existente pelo ID.
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping(value = "/{id}")
    public ResponseEntity<UserDTO> update(@PathVariable Long id, @Valid @RequestBody UserUpdateDTO dto){
        // @PathVariable Long id: Obtém o ID do usuário da URL.
        // @Valid: Ativa a validação dos campos do DTO de atualização.
        // @RequestBody: Mapeia o corpo da requisição HTTP para o objeto UserUpdateDTO.
        // Chama o serviço para atualizar o usuário com o ID fornecido.
        UserDTO newDto = service.update(id, dto);
        // Retorna uma resposta HTTP 200 OK com o usuário atualizado no corpo.
        return ResponseEntity.ok().body(newDto);
    }

    // @PreAuthorize("hasRole('ROLE_ADMIN')"): Garante que apenas usuários com o papel 'ROLE_ADMIN' podem acessar este método.
    // Deleta um usuário pelo ID.
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping(value = "/{id}")
    public ResponseEntity<UserDTO> delete(@PathVariable Long id){
        // @PathVariable Long id: Obtém o ID do usuário da URL.
        // Chama o serviço para excluir o usuário com o ID fornecido.
        service.delete(id);
        // Retorna uma resposta HTTP 204 No Content, indicando que a operação foi bem-sucedida e não há conteúdo para retornar.
        return ResponseEntity.noContent().build();
    }
}