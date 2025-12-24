package com.devsuprior.dscatalog.services;

import com.devsuprior.dscatalog.dto.CategoryDTO;
import com.devsuprior.dscatalog.entities.Category;
import com.devsuprior.dscatalog.repositories.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    //Coloca a anotação Autowired para fazer a injeção de dependencia da classe CategoryRepository
    @Autowired
    private CategoryRepository repository;

    @Transactional(readOnly = true)
    public List<CategoryDTO> findAll() {
        List<Category> list = repository.findAll();
        //Fazendo com lambda
        //O map faz a conversão de cada elemento Category em CategoryDTO
        List<CategoryDTO> listDto = list.stream().map( x-> new CategoryDTO(x))
                //O collect converte stream para lista
                .collect(Collectors.toList());
        //Fazendo com for
        /* Uma forma de colocar os elementos Category na lista Category DTO
        List<CategoryDTO> listDto = new ArrayList<>();
        for(Category cat: list){
            listDto.add(new CategoryDTO(cat));
        }*/
        return listDto;
    }
    @Transactional(readOnly = true)
    public CategoryDTO findById(Long id) {
        Optional<Category> obj = repository.findById(id);
        Category entity = obj.get();
        return new CategoryDTO(entity);
    }
}
