package com.devsuperior.dscatalog.resources;

import com.devsuperior.dscatalog.dto.ProductDTO;
import com.devsuperior.dscatalog.services.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import org.springframework.data.domain.Pageable; // <-- CORRIGIDO: Importação correta do Pageable
import java.net.URI;

//Controla as requisições da api
// @RestController: Indica que esta classe é um controlador REST, capaz de lidar com requisições HTTP.
// @RequestMapping(value = "/products"): Define o caminho base para todos os endpoints neste controlador.
@RestController
@RequestMapping(value = "/products")
public class ProductResource {

    //Coloca a anotação Autowired para fazer a injeção de dependencia da classe ProductService
    // @Autowired: Injeta uma instância de ProductService, que contém a lógica de negócio para produtos.
    @Autowired
    private ProductService service;

    // @GetMapping: Mapeia requisições HTTP GET para o caminho base "/products".
    // public ResponseEntity<Page<ProductDTO>> findAll(Pageable pageable): Retorna uma lista paginada de produtos.
    @GetMapping
    public ResponseEntity<Page<ProductDTO>> findAll(
            @RequestParam(value = "name", defaultValue = "") String name,
            @RequestParam(value = "categoryId", defaultValue = "0")String categoryId,
            Pageable pageable){

        // PARAMETROS: page, size, sort
        // O list está buscando os registros da classe ProductService pelo método findAll
        // Chama o serviço para buscar todos os produtos de forma paginada.
        Page<ProductDTO> list = service.findAllPaged(name, categoryId, pageable);
        // Retorna uma resposta HTTP 200 OK com a lista de produtos no corpo.
        return ResponseEntity.ok().body(list);
    }

    // @GetMapping(value = "/{id}"): Mapeia requisições HTTP GET para "/products/{id}", onde {id} é um parâmetro de caminho.
    // public ResponseEntity<ProductDTO> findById(@PathVariable Long id): Retorna um produto específico pelo ID.
    @GetMapping(value = "/{id}")
    public ResponseEntity<ProductDTO> findById(@PathVariable Long id){
        // O list está buscando os registros da classe ProductService pelo método findAll
        // Chama o serviço para buscar um produto pelo ID.
        ProductDTO dto = service.findById(id);
        // Retorna uma resposta HTTP 200 OK com o produto encontrado no corpo.
        return ResponseEntity.ok().body(dto);
    }

    // @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_OPERATOR')"): Garante que apenas usuários com os papéis ADMIN ou OPERATOR podem acessar este método.
    // @PostMapping: Mapeia requisições HTTP POST para o caminho base "/products".
    // public ResponseEntity<ProductDTO> insert(@Valid @RequestBody ProductDTO dto): Insere um novo produto.
    // @Valid: Ativa a validação dos campos do DTO.
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_OPERATOR')")
    @PostMapping
    public ResponseEntity<ProductDTO> insert(@Valid @RequestBody ProductDTO dto){
        // Chama o serviço para inserir o novo produto.
        dto = service.insert(dto);
        // Constrói a URI para o novo recurso criado (ex: /products/1).
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(dto.getId()).toUri();
        // Retorna uma resposta HTTP 201 Created com a URI do novo recurso e o produto criado no corpo.
        return ResponseEntity.created(uri).body(dto);
    }

    // @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_OPERATOR')"): Garante que apenas usuários com os papéis ADMIN ou OPERATOR podem acessar este método.
    // @PutMapping(value = "/{id}"): Mapeia requisições HTTP PUT para "/products/{id}".
    // public ResponseEntity<ProductDTO> update(@PathVariable Long id, @Valid @RequestBody ProductDTO dto): Atualiza um produto existente.
    // @Valid: Ativa a validação dos campos do DTO.
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_OPERATOR')")
    @PutMapping(value = "/{id}")
    public ResponseEntity<ProductDTO> update(@PathVariable Long id, @Valid @RequestBody ProductDTO dto){
        // Chama o serviço para atualizar o produto com o ID fornecido.
        dto = service.update(id, dto);
        // Retorna uma resposta HTTP 200 OK com o produto atualizado no corpo.
        return ResponseEntity.ok().body(dto);
    }

    // @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_OPERATOR')"): Garante que apenas usuários com os papéis ADMIN ou OPERATOR podem acessar este método.
    // @DeleteMapping(value = "/{id}"): Mapeia requisições HTTP DELETE para "/products/{id}".
    // public ResponseEntity<ProductDTO> delete(@PathVariable Long id): Exclui um produto existente.
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_OPERATOR')")
    @DeleteMapping(value = "/{id}")
    public ResponseEntity<ProductDTO> delete(@PathVariable Long id){
        // Chama o serviço para excluir o produto com o ID fornecido.
        service.delete(id);
        // Retorna uma resposta HTTP 204 No Content, indicando que a operação foi bem-sucedida e não há conteúdo para retornar.
        return ResponseEntity.noContent().build();
    }

}