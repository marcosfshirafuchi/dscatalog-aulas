package com.devsuprior.dscatalog.services;

import com.devsuprior.dscatalog.exceptions.ResourceNotFoundException;
import com.devsuprior.dscatalog.repositories.ProductRepository;

// JUnit 5
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

// Mockito + JUnit 5
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

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

    @BeforeEach
    void setUp() {
        // Inicializa os valores usados nos testes
        // Executa antes de cada método @Test
        existingId = 1L;
        nonExistingId = 2L;
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
