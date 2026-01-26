package com.devsuprior.dscatalog.services;

import com.devsuprior.dscatalog.entities.Product;
import com.devsuprior.dscatalog.exceptions.DatabaseException;
import com.devsuprior.dscatalog.exceptions.ResourceNotFoundException;
import com.devsuprior.dscatalog.repositories.ProductRepository;

// JUnit 5
import com.devsuprior.dscatalog.tests.Factory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

// Mockito + JUnit 5
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.mockito.Mockito.*;

// Ativa o Mockito para inicializar automaticamente
// os mocks (@Mock) e injetá-los no service (@InjectMocks)
@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    // =========================
    // DEPENDÊNCIA SOB TESTE
    // =========================

    // Cria a instância REAL do ProductService
    // e injeta automaticamente os mocks declarados nesta classe
    @InjectMocks
    private ProductService service;

    // =========================
    // DEPENDÊNCIAS MOCKADAS
    // =========================

    // Cria um mock do ProductRepository
    // Nenhuma chamada real ao banco de dados será feita
    @Mock
    private ProductRepository repository;

    // =========================
    // DADOS DE APOIO (TEST FIXTURES)
    // =========================

    // ID que representa um produto EXISTENTE no banco
    private long existingId;

    // ID que representa um produto INEXISTENTE no banco
    private long nonExistingId;

    // ID de um produto que tem dependência (integridade referencial)
    private long dependentId;

    // Objeto Page do Spring Data para simular retorno paginado
    private PageImpl<Product> page;
    
    // Entidade Product usada nos testes
    private Product product;

    @BeforeEach
    void setUp() {
        // Inicializa os valores usados nos testes
        // Executa antes de cada método @Test
        existingId = 1L;
        nonExistingId = 1000L;
        dependentId = 4L;
        
        // Cria uma instância de produto válida
        product = Factory.createProduct();
        
        // Cria uma página contendo o produto instanciado
        page = new PageImpl<>(List.of(product));
        
        // Configura o mock para retornar a página criada quando buscar todos
        Mockito.lenient().when(repository.findAll((Pageable)ArgumentMatchers.any())).thenReturn(page);
        
        // Configura o mock para retornar o produto quando salvar
        Mockito.lenient().when(repository.save(ArgumentMatchers.any())).thenReturn(product);

        // Configura o mock para retornar o produto (Optional.of) quando buscar pelo ID existente
        Mockito.lenient().when(repository.findById(existingId)).thenReturn(java.util.Optional.of(product));

        // Configura o mock para retornar vazio (Optional.empty) quando buscar pelo ID inexistente
        Mockito.lenient().when(repository.findById(nonExistingId)).thenReturn(java.util.Optional.empty());
    }

    @Test
    void deleteShouldDoNothingWhenIdExists() {

        // =========================
        // ARRANGE (Preparação)
        // =========================
        // Define o comportamento do mock para este cenário:
        // - o produto existe
        // - o delete não lança exceção
        Mockito.when(repository.existsById(existingId)).thenReturn(true);
        Mockito.doNothing().when(repository).deleteById(existingId);

        // =========================
        // ACT + ASSERT (Ação + Verificação)
        // =========================
        // Executa o método que está sendo testado
        // e valida que nenhuma exceção é lançada
        Assertions.assertDoesNotThrow(() -> service.delete(existingId));

        // =========================
        // ASSERT (Verificações adicionais)
        // =========================
        // Garante que o service:
        // 1) verificou se o produto existia
        // 2) chamou o delete corretamente
        Mockito.verify(repository, times(1)).existsById(existingId);
        Mockito.verify(repository, times(1)).deleteById(existingId);
    }

    @Test
    public void deleteShouldThrowDatabaseExceptionWhenDepedentId(){

        // =========================
        // ARRANGE (Preparação)
        // =========================
        // Configura o mock para simular um erro de integridade referencial
        // (ex: tentar apagar um produto que tem pedidos associados)
        Mockito.doThrow(DataIntegrityViolationException.class).when(repository).deleteById(dependentId);
        Mockito.when(repository.existsById(dependentId)).thenReturn(true);

        // =========================
        // ACT + ASSERT (Ação + Verificação)
        // =========================
        // Verifica se o service captura a exceção de banco e lança
        // a exceção personalizada da aplicação (DatabaseException)
        Assertions.assertThrows(DatabaseException.class, () -> {
            service.delete(dependentId);
        });

        // =========================
        // ASSERT (Verificações adicionais)
        // =========================
        // Garante que o método deleteById foi chamado
        Mockito.verify(repository, times(1)).deleteById(dependentId);
    }

    @Test
    void deleteShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {

        // =========================
        // ARRANGE (Preparação)
        // =========================
        // Define o comportamento do mock:
        // - o produto NÃO existe
        Mockito.when(repository.existsById(nonExistingId)).thenReturn(false);

        // =========================
        // ACT + ASSERT (Ação + Verificação)
        // =========================
        // Executa o delete com um ID inexistente
        // e valida que a exceção esperada é lançada
        Assertions.assertThrows(ResourceNotFoundException.class, () -> {
            service.delete(nonExistingId);
        });

        // (Opcional, mas recomendado)
        // Garante que o delete NÃO foi chamado
        Mockito.verify(repository, times(1)).existsById(nonExistingId);
        Mockito.verify(repository, never()).deleteById(anyLong());
    }
}