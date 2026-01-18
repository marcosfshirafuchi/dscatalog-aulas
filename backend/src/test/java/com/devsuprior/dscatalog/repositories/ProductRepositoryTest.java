package com.devsuprior.dscatalog.repositories;

import com.devsuprior.dscatalog.entities.Product;
import com.devsuprior.dscatalog.tests.Factory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.Optional;

@DataJpaTest
public class ProductRepositoryTest {

    @Autowired
    private ProductRepository repository;

    private long existingId;
    private long nonExistingId;
    private long countTotalProducts;

    @BeforeEach
    void setUp() throws Exception{
        existingId = 1L;
        nonExistingId = 1000L;
        countTotalProducts = 25L;
    }

    @Test
    public void saveShouldPersistWithAutoIncrementWhenIdIsNull(){

        //●	Act: execute as ações necessárias
        Product product = Factory.createProduct();
        product.setId(null);

        product = repository.save(product);

        //●	Assert: declare o que deveria acontecer (resultado esperado)
        Assertions.assertNotNull(product.getId());
        Assertions.assertEquals(countTotalProducts + 1, product.getId());
    }

    @Test
    public void deleteShouldDeleteObjectWhenIdExists(){

        //Padrão AAA
        //●	Arrange: instancie os objetos necessários
        //long existing = 1L;

        //●	Act: execute as ações necessárias
        repository.deleteById(existingId);

        //●	Assert: declare o que deveria acontecer (resultado esperado)
        Optional<Product> result = repository.findById(existingId);
        Assertions.assertFalse(result.isPresent());
    }

    @Test
    public void findByIdShouldReturnAnOptionalProductNotEmptyWhenIdExists(){
        //●	Act: execute as ações necessária
        Optional<Product> result = repository.findById(existingId);

        //●	Assert: declare o que deveria acontecer (resultado esperado)
        Assertions.assertTrue(result.isPresent());
    }


    @Test
    public void findByIdShouldReturnAnEmptyOptionalProductWhenTheIdDoesNotExist(){
        //●	Act: execute as ações necessária
        Optional<Product> result = repository.findById(nonExistingId);

        //●	Assert: declare o que deveria acontecer (resultado esperado)
        Assertions.assertTrue(result.isEmpty());
    }
}