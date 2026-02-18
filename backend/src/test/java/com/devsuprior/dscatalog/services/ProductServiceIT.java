package com.devsuprior.dscatalog.services;

import com.devsuprior.dscatalog.dto.ProductDTO;
import com.devsuprior.dscatalog.exceptions.ResourceNotFoundException;
import com.devsuprior.dscatalog.repositories.ProductRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

// @SpringBootTest: Carrega o contexto completo da aplicação Spring Boot.
// Isso significa que todos os beans, configurações e o banco de dados (H2 em memória, geralmente) serão inicializados.
// É usado para testes de integração (IT - Integration Test).
@SpringBootTest
@Transactional // Garante que cada teste seja executado dentro de uma transação e que o banco seja revertido (rollback) ao final de cada teste.
public class ProductServiceIT {

    @Autowired
    private ProductService service;

    @Autowired
    private ProductRepository repository;

    private Long existingId;
    private Long nonExistingId;
    private Long countTotalProducts;

    @BeforeEach
    void setUp() throws Exception {
        existingId = 1L;
        nonExistingId = 1000L;
        countTotalProducts = 25L;
    }

    @Test
    public void deleteShouldDeleteResourceWhenIdExists(){
        // ARRANGE: instancie os objetos necessários
        // (Os IDs e a contagem inicial já foram configurados no setUp)

        // ACT: execute as ações necessárias
        // Deleta o produto com ID existente
        service.delete(existingId);

        // ASSERT: declare o que deveria acontecer (resultado esperado)
        // Verifica se a contagem total de produtos diminuiu em 1
        Assertions.assertEquals(countTotalProducts - 1, repository.count());
    }

    @Test
    public void deleteShouldThrowResourceNotFoundExceptionWhenDoesNotIdExists(){
        // ARRANGE: instancie os objetos necessários
        // (O ID não existente já foi configurado no setUp)

        // ACT & ASSERT: execute as ações e verifique o resultado
        // Verifica se a exceção ResourceNotFoundException é lançada ao tentar deletar um ID inexistente
        Assertions.assertThrows(ResourceNotFoundException.class, () -> {
            service.delete(nonExistingId);
        });
    }

    @Test
    public void findAllPagedShouldReturnPageWhenPage0Size10(){
        // ARRANGE: instancie os objetos necessários
        // Cria um objeto PageRequest para solicitar a primeira página (0) com 10 itens
        PageRequest pageRequest = PageRequest.of(0,10);

        // ACT: execute as ações necessárias
        // Chama o método findAllPaged do serviço
        Page<ProductDTO> result = service.findAllPaged(pageRequest);

        // ASSERT: declare o que deveria acontecer (resultado esperado)
        // Verifica se a página retornada não está vazia
        Assertions.assertFalse(result.isEmpty());
        // Verifica se o número da página é 0
        Assertions.assertEquals(0, result.getNumber());
        // Verifica se o tamanho da página é 10
        Assertions.assertEquals(10,result.getSize());
        // Verifica se o total de elementos corresponde ao total esperado no banco
        Assertions.assertEquals(countTotalProducts, result.getTotalElements());
    }

    @Test
    public void findAllPagedShouldReturnEmptyPageWhenPageDoesNotExist(){
        // ARRANGE: instancie os objetos necessários
        // Cria um objeto PageRequest para solicitar uma página inexistente (50)
        PageRequest pageRequest = PageRequest.of(50,10);

        // ACT: execute as ações necessárias
        // Chama o método findAllPaged do serviço
        Page<ProductDTO> result = service.findAllPaged(pageRequest);

        // ASSERT: declare o que deveria acontecer (resultado esperado)
        // Verifica se a página retornada está vazia
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    public void findAllPagedShouldReturnSortedPageWhenSortByName(){
        // ARRANGE: instancie os objetos necessários
        // Cria um objeto PageRequest para solicitar a primeira página com 10 itens, ordenada pelo nome
        PageRequest pageRequest = PageRequest.of(0,10, Sort.by("name"));

        // ACT: execute as ações necessárias
        // Chama o método findAllPaged do serviço
        Page<ProductDTO> result = service.findAllPaged(pageRequest);

        // ASSERT: declare o que deveria acontecer (resultado esperado)
        // Verifica se a página retornada não está vazia
        Assertions.assertFalse(result.isEmpty());
        // Verifica se os primeiros itens estão ordenados corretamente por nome
        Assertions.assertEquals("Macbook Pro", result.getContent().get(0).getName());
        Assertions.assertEquals("PC Gamer", result.getContent().get(1).getName());
        Assertions.assertEquals("PC Gamer Alfa", result.getContent().get(2).getName());
    }
}