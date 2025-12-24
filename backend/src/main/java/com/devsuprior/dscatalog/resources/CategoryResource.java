package com.devsuprior.dscatalog.resources;

import com.devsuprior.dscatalog.dto.CategoryDTO;
import com.devsuprior.dscatalog.entities.Category;
import com.devsuprior.dscatalog.services.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

//Controla as requisições da api
@RestController
@RequestMapping(value = "/categories")
public class CategoryResource {

    //Coloca a anotação Autowired para fazer a injeção de dependencia da classe CategoryService
    @Autowired
    private CategoryService service;

    @GetMapping
    public ResponseEntity<List<CategoryDTO>> findAll(){
        // O list está buscando os registros da classe CategoryService pelo método findAll
        List<CategoryDTO> list = service.findAll();
        return ResponseEntity.ok().body(list);
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<CategoryDTO> findById(@PathVariable Long id){
        // O list está buscando os registros da classe CategoryService pelo método findAll
       CategoryDTO dto = service.findById(id);
        return ResponseEntity.ok().body(dto);
    }
}
