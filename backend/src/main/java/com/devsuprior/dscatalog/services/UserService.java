package com.devsuprior.dscatalog.services;

import com.devsuprior.dscatalog.dto.*;
import com.devsuprior.dscatalog.entities.Category;
import com.devsuprior.dscatalog.entities.Product;
import com.devsuprior.dscatalog.entities.Role;
import com.devsuprior.dscatalog.entities.User;
import com.devsuprior.dscatalog.exceptions.DatabaseException;
import com.devsuprior.dscatalog.exceptions.ResourceNotFoundException;
import com.devsuprior.dscatalog.repositories.RoleRepository;
import com.devsuprior.dscatalog.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    //Coloca a anotação Autowired para fazer a injeção de dependencia da classe ProductRepository
    @Autowired
    private UserRepository repository;

    @Autowired
    private RoleRepository roleRepository;

    @Transactional(readOnly = true)
    public Page<UserDTO> findAllPaged(Pageable pageable) {
        Page<User> list = repository.findAll(pageable);
        //Fazendo com lambda
        //O map faz a conversão de cada elemento Product em ProductDTO
        Page<UserDTO> listDto = list.map(x-> new UserDTO(x));
        //Fazendo com for
        /* Uma forma de colocar os elementos Product na lista Product DTO
        List<ProductDTO> listDto = new ArrayList<>();
        for(Product prod: list){
            listDto.add(new CategoryDTO(prod));
        }*/
        return listDto;
    }
    @Transactional(readOnly = true)
    public UserDTO findById(Long id) {
        Optional<User> obj = repository.findById(id);
        //O orElseThrow vai permitir uma chamada de exceção caso não encontre o id
        User entity = obj.orElseThrow(() -> new ResourceNotFoundException("Entity not found"));
        return new UserDTO(entity);
    }

    @Transactional
    public UserDTO insert(UserInsertDTO dto) {
        User entity = new User();
        copyDtoToEntity(dto, entity);
        entity.setPassword(passwordEncoder.encode(dto.getPassword()));
        entity = repository.save(entity);
        return new UserDTO(entity);
    }

    @Transactional
    public UserDTO update(Long id, UserDTO dto) {
        try {
            User entity = repository.getReferenceById(id);
            copyDtoToEntity(dto, entity);
            entity = repository.save(entity);
            return new UserDTO(entity);
        } catch (EntityNotFoundException e) {
            throw new ResourceNotFoundException("Id not found " + id);
        }
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Recurso não encotntrado");
        }
        try {
            repository.deleteById(id);
        }
        catch (DataIntegrityViolationException e) {
            throw new DatabaseException("Falha de integridade referencial");
        }
    }

    private void copyDtoToEntity(UserDTO dto, User entity) {
        entity.setFirstName(dto.getFirstName());
        entity.setLastName(dto.getLastName());
        entity.setEmail(dto.getEmail());

        entity.getRoles().clear();
        for (RoleDTO roleDto: dto.getRoles()){
            Role role = roleRepository.getReferenceById(roleDto.getId());
            entity.getRoles().add(role);
        }
    }
}
