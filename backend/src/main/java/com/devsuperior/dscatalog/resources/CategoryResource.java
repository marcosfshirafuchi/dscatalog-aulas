package com.devsuperior.dscatalog.resources;

import com.devsuperior.dscatalog.dto.CategoryDTO;
import com.devsuperior.dscatalog.services.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

//Controla as requisições da api
// @RestController: Indica que esta classe é um controlador REST, capaz de lidar com requisições HTTP.
// @RequestMapping(value = "/categories"): Define o caminho base para todos os endpoints neste controlador.
@RestController
@RequestMapping(value = "/categories")
public class CategoryResource {

    //Coloca a anotação Autowired para fazer a injeção de dependencia da classe CategoryService
    // @Autowired: Injeta uma instância de CategoryService, que contém a lógica de negócio para categorias.
    @Autowired
    private CategoryService service;

    // @GetMapping: Mapeia requisições HTTP GET para o caminho base "/categories".
    // public ResponseEntity<Page<CategoryDTO>> findAll(Pageable pageable): Retorna uma lista paginada de categorias.
    @GetMapping
    public ResponseEntity<Page<CategoryDTO>> findAll(Pageable pageable){
        // O list está buscando os registros da classe CategoryService pelo método findAll
        // Chama o serviço para buscar todas as categorias de forma paginada.
        Page<CategoryDTO> list = service.findAllPaged(pageable);
        // Retorna uma resposta HTTP 200 OK com a lista de categorias no corpo.
        return ResponseEntity.ok().body(list);
    }

    // @GetMapping(value = "/{id}"): Mapeia requisições HTTP GET para "/categories/{id}", onde {id} é um parâmetro de caminho.
    // public ResponseEntity<CategoryDTO> findById(@PathVariable Long id): Retorna uma categoria específica pelo ID.
    @GetMapping(value = "/{id}")
    public ResponseEntity<CategoryDTO> findById(@PathVariable Long id){
        // O list está buscando os registros da classe CategoryService pelo método findAll
        // Chama o serviço para buscar uma categoria pelo ID.
        CategoryDTO dto = service.findById(id);
        // Retorna uma resposta HTTP 200 OK com a categoria encontrada no corpo.
        return ResponseEntity.ok().body(dto);
    }

    // @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_OPERATOR')"): Garante que apenas usuários com os papéis ADMIN ou OPERATOR podem acessar este método.
    // @PostMapping: Mapeia requisições HTTP POST para o caminho base "/categories".
    // public ResponseEntity<CategoryDTO> insert(@RequestBody CategoryDTO dto): Insere uma nova categoria.
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_OPERATOR')")
    @PostMapping
    public ResponseEntity<CategoryDTO> insert(@RequestBody CategoryDTO dto){
        // Chama o serviço para inserir a nova categoria.
        dto = service.insert(dto);
        // Constrói a URI para o novo recurso criado (ex: /categories/1).
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(dto.getId()).toUri();
        // Retorna uma resposta HTTP 201 Created com a URI do novo recurso e a categoria criada no corpo.
        return ResponseEntity.created(uri).body(dto);
    }

    // @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_OPERATOR')"): Garante que apenas usuários com os papéis ADMIN ou OPERATOR podem acessar este método.
    // @PutMapping(value = "/{id}"): Mapeia requisições HTTP PUT para "/categories/{id}".
    // public ResponseEntity<CategoryDTO> update(@PathVariable Long id,@RequestBody CategoryDTO dto): Atualiza uma categoria existente.
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_OPERATOR')")
    @PutMapping(value = "/{id}")
    public ResponseEntity<CategoryDTO> update(@PathVariable Long id,@RequestBody CategoryDTO dto){
        // Chama o serviço para atualizar a categoria com o ID fornecido.
        dto = service.update(id, dto);
        // Retorna uma resposta HTTP 200 OK com a categoria atualizada no corpo.
        return ResponseEntity.ok().body(dto);
    }

    // @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_OPERATOR')"): Garante que apenas usuários com os papéis ADMIN ou OPERATOR podem acessar este método.
    // @DeleteMapping(value = "/{id}"): Mapeia requisições HTTP DELETE para "/categories/{id}".
    // public ResponseEntity<CategoryDTO> delete(@PathVariable Long id): Exclui uma categoria existente.
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_OPERATOR')")
    @DeleteMapping(value = "/{id}")
    public ResponseEntity<CategoryDTO> delete(@PathVariable Long id){
        // Chama o serviço para excluir a categoria com o ID fornecido.
        service.delete(id);
        // Retorna uma resposta HTTP 204 No Content, indicando que a operação foi bem-sucedida e não há conteúdo para retornar.
        return ResponseEntity.noContent().build();
    }

}