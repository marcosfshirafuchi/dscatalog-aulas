package com.devsuprior.dscatalog.repositories;

import com.devsuprior.dscatalog.entities.Product;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.Optional;

@DataJpaTest
public class ProductRepositoryTest {

    @Autowired
    private ProductRepository repository;

    @Test
    public void deleteShouldDeleteObjectWhenIdExists(){

        //Padrão AAA
        //●	Arrange: instancie os objetos necessários
        long existing = 1L;

        //●	Act: execute as ações necessárias
        repository.deleteById(existing);

        //●	Assert: declare o que deveria acontecer (resultado esperado)
        Optional<Product> result = repository.findById(existing);
        Assertions.assertFalse(result.isPresent());

    }
}