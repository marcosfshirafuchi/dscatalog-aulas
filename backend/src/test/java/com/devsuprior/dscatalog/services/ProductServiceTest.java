package com.devsuprior.dscatalog.services;

// Repositório que será mockado (não acessa banco real)
import com.devsuprior.dscatalog.repositories.ProductRepository;

// JUnit 5
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

// Integração do Mockito com JUnit 5
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

// Ativa o Mockito para inicializar automaticamente @Mock e @InjectMocks
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    // Cria a instância real do ProductService
    // e injeta automaticamente os mocks (@Mock) nele
    @InjectMocks
    private ProductService service;

    // Cria um mock do ProductRepository
    // Nenhuma chamada real ao banco será feita
    @Mock
    private ProductRepository repository;

    // ID que simula um produto existente no banco
    private long existingId;

    // ID que simula um produto inexistente
    // (não é usado neste teste específico)
    private long nonExistingId;

    @BeforeEach
    void setUp() {

        // Inicializa os valores dos IDs antes de cada teste
        existingId = 1L;
        nonExistingId = 1000L;

        // Configura o comportamento do mock:
        // quando o service chamar repository.existsById(1L),
        // o mock responderá "true", simulando que o produto existe
        Mockito.when(repository.existsById(existingId)).thenReturn(true);

        // Esta linha foi comentada porque:
        // - ESTE teste não usa nonExistingId
        // - O método delete(existingId) nunca chama existsById(1000L)
        // - Logo, esse stub é desnecessário aqui
        //
        // Mockito.when(repository.existsById(nonExistingId)).thenReturn(false);

        // Simula que o delete funciona normalmente
        // (não lança exceção quando o ID existe)
        Mockito.doNothing().when(repository).deleteById(existingId);
    }

    @Test
    void deleteShouldDoNothingWhenIdExists() {

        // Verifica que o método delete NÃO lança exceção
        // quando o ID existe
        Assertions.assertDoesNotThrow(() -> service.delete(existingId));

        // Garante que o service realmente verificou
        // se o produto existia antes de deletar
        Mockito.verify(repository, Mockito.times(1))
                .existsById(existingId);

        // Garante que o delete foi chamado exatamente 1 vez
        Mockito.verify(repository, Mockito.times(1))
                .deleteById(existingId);
    }
}
