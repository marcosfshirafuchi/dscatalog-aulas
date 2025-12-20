package com.devsuprior.dscatalog.services;

import com.devsuprior.dscatalog.entities.Category;
import com.devsuprior.dscatalog.repositories.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    //Coloca a anotação Autowired para fazer a injeção de dependencia da classe CategoryRepository
    @Autowired
    private CategoryRepository repository;

    public List<Category> findAll() {
        return repository.findAll();
    }

}
