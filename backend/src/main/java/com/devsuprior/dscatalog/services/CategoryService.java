package com.devsuprior.dscatalog.services;

import com.devsuprior.dscatalog.dto.CategoryDTO;
import com.devsuprior.dscatalog.entities.Category;
import com.devsuprior.dscatalog.exceptions.DatabaseException;
import com.devsuprior.dscatalog.exceptions.ResourceNotFoundException;
import com.devsuprior.dscatalog.repositories.CategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    //Coloca a anotação Autowired para fazer a injeção de dependencia da classe CategoryRepository
    @Autowired
    private CategoryRepository repository;

    @Transactional(readOnly = true)
    public Page<CategoryDTO> findAllPaged(PageRequest pageRequest) {
        Page<Category> list = repository.findAll(pageRequest);
        //Fazendo com lambda
        //O map faz a conversão de cada elemento Category em CategoryDTO
        Page<CategoryDTO> listDto = list.map( x-> new CategoryDTO(x));
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
        //O orElseThrow vai permitir uma chamada de exceção caso não encontre o id
        Category entity = obj.orElseThrow(() -> new ResourceNotFoundException("Entity not found"));
        return new CategoryDTO(entity);
    }

    @Transactional(readOnly = true)
    public CategoryDTO insert(CategoryDTO dto) {
        Category entity = new Category();
        entity.setName(dto.getName());
        entity = repository.save(entity);
        return new CategoryDTO(entity);
    }

    @Transactional
    public CategoryDTO update(Long id, CategoryDTO dto) {
        try {
            Category entity = repository.getReferenceById(id);
            entity.setName(dto.getName());
            entity = repository.save(entity);
            return new CategoryDTO(entity);
        } catch (EntityNotFoundException e) {
            throw new ResourceNotFoundException("Id not found " + id);
        }
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Recurso não encontrado");
        }
        try {
            repository.deleteById(id);
        }
        catch (DataIntegrityViolationException e) {
            throw new DatabaseException("Falha de integridade referencial");
        }
    }
}
