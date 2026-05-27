package com.devsuperior.dscatalog.services;

import com.devsuperior.dscatalog.dto.RoleDTO;
import com.devsuperior.dscatalog.dto.UserDTO;
import com.devsuperior.dscatalog.dto.UserInsertDTO;
import com.devsuperior.dscatalog.dto.UserUpdateDTO;
import com.devsuperior.dscatalog.entities.Role;
import com.devsuperior.dscatalog.entities.User;
import com.devsuperior.dscatalog.projections.UserDetailsProjection;
import com.devsuperior.dscatalog.resources.exceptions.DatabaseException;
import com.devsuperior.dscatalog.resources.exceptions.ResourceNotFoundException;
import com.devsuperior.dscatalog.repositories.RoleRepository;
import com.devsuperior.dscatalog.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

// @Service: Indica que esta classe é um componente de serviço do Spring,
// responsável pela lógica de negócio e transações.
// Implements UserDetailsService: Indica que esta classe é responsável por carregar
// detalhes do usuário para autenticação e autorização do Spring Security.
@Service
public class UserService implements UserDetailsService {

    // @Autowired: Injeta uma instância de PasswordEncoder (configurada em AppConfig)
    // para codificar senhas antes de salvá-las no banco de dados.
    @Autowired
    private PasswordEncoder passwordEncoder;

    // @Autowired: Injeta uma instância de UserRepository para realizar operações de
    // persistência (CRUD) na entidade User.
    @Autowired
    private UserRepository repository;

    // @Autowired: Injeta uma instância de RoleRepository para buscar e manipular roles.
    @Autowired
    private RoleRepository roleRepository;

    // @Transactional(readOnly = true): Indica que o método é transacional e apenas de leitura.
    // Otimiza a performance, pois não precisa gerenciar transações de escrita.
    public Page<UserDTO> findAllPaged(Pageable pageable) {
        // Busca todos os usuários de forma paginada do repositório.
        Page<User> list = repository.findAll(pageable);
        // Converte a Page de entidades User para uma Page de UserDTOs usando uma expressão lambda.
        Page<UserDTO> listDto = list.map(x-> new UserDTO(x));
        return listDto;
    }

    // @Transactional(readOnly = true): Indica que o método é transacional e apenas de leitura.
    public UserDTO findById(Long id) {
        // Busca um usuário pelo ID. Retorna um Optional<User>.
        Optional<User> obj = repository.findById(id);
        // orElseThrow: Se o Optional estiver vazio (usuário não encontrado), lança uma
        // ResourceNotFoundException. Caso contrário, retorna a entidade User.
        User entity = obj.orElseThrow(() -> new ResourceNotFoundException("Entity not found"));
        // Converte a entidade User encontrada para UserDTO e a retorna.
        return new UserDTO(entity);
    }

    // @Transactional: Indica que o método é transacional. Todas as operações de banco de dados
    // dentro deste método serão executadas em uma única transação.
    public UserDTO insert(UserInsertDTO dto) {
        // Cria uma nova instância de User.
        User entity = new User();
        // Copia os dados do DTO para a entidade User.
        copyDtoToEntity(dto, entity);
        // Codifica a senha do DTO usando o PasswordEncoder e a define na entidade.
        entity.setPassword(passwordEncoder.encode(dto.getPassword()));
        // Salva a nova entidade User no banco de dados.
        entity = repository.save(entity);
        // Converte a entidade salva para UserDTO e a retorna.
        return new UserDTO(entity);
    }

    // @Transactional: Indica que o método é transacional.
    public UserDTO update(Long id, UserUpdateDTO dto) {
        try {
            // getReferenceById: Obtém uma referência à entidade User sem carregá-la completamente do banco.
            // Isso é útil para atualizações, pois o JPA pode gerenciar a entidade.
            User entity = repository.getReferenceById(id);
            // Copia os dados do DTO para a entidade User.
            copyDtoToEntity(dto, entity);
            // Salva as alterações na entidade User no banco de dados.
            entity = repository.save(entity);
            // Converte a entidade atualizada para UserDTO e a retorna.
            return new UserDTO(entity);
        } catch (EntityNotFoundException e) {
            // Se getReferenceById não encontrar a entidade, EntityNotFoundException é lançada.
            // Captura e relança como ResourceNotFoundException personalizada.
            throw new ResourceNotFoundException("Id not found " + id);
        }
    }

    // @Transactional(propagation = Propagation.SUPPORTS): Indica que o método suporta uma transação existente.
    // Se não houver uma transação, ele será executado sem uma.
    // No entanto, para operações de exclusão, o Spring Data JPA geralmente exige uma transação.
    public void delete(Long id) {
        // Verifica se o usuário com o ID fornecido existe.
        if (!repository.existsById(id)) {
            // Se não existir, lança uma ResourceNotFoundException.
            throw new ResourceNotFoundException("Recurso não encotntrado");
        }
        try {
            // Tenta deletar o usuário pelo ID.
            repository.deleteById(id);
        }
        catch (DataIntegrityViolationException e) {
            // Captura DataIntegrityViolationException (ex: se o usuário tiver dados relacionados)
            // e relança como DatabaseException personalizada.
            throw new DatabaseException("Falha de integridade referencial");
        }
    }

    // Método auxiliar para copiar dados de um UserDTO (ou UserInsertDTO/UserUpdateDTO) para uma entidade User.
    private void copyDtoToEntity(UserDTO dto, User entity) {
        entity.setFirstName(dto.getFirstName());
        entity.setLastName(dto.getLastName());
        entity.setEmail(dto.getEmail());

        // Limpa os roles existentes da entidade para adicionar os novos do DTO.
        entity.getRoles().clear();
        // Itera sobre os RoleDTOs do DTO.
        for (RoleDTO roleDto: dto.getRoles()){
            // Busca a entidade Role pelo ID do RoleDTO. Usa findById para carregar a entidade completamente e lança uma exceção se não encontrada.
            Role role = roleRepository.findById(roleDto.getId())
                                      .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + roleDto.getId()));
            // Adiciona a entidade Role à coleção de roles do usuário.
            entity.getRoles().add(role);
        }
    }

    // loadUserByUsername: Implementação do método da interface UserDetailsService.
    // Usado pelo Spring Security para carregar os detalhes de um usuário durante o processo de autenticação.
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Busca o usuário e seus roles pelo email (username) usando uma projeção personalizada.
        List<UserDetailsProjection> result = repository.searchUserAndRolesByEmail(username);
        // Se nenhum resultado for encontrado, o email não existe.
        if (result.size() == 0) {
            throw new UsernameNotFoundException("Email not found");
        }
        // Cria uma nova instância de User (entidade) e preenche com os dados da projeção.
        User user = new User();
        user.setEmail(username);
        user.setPassword(result.get(0).getPassword()); // A senha é a mesma para todas as projeções do mesmo usuário.
        // Adiciona os roles do usuário com base nas projeções.
        for (UserDetailsProjection projection : result) {
            user.addRole(new Role(projection.getRoleId(), projection.getAuthority()));
        }
        // Retorna a entidade User, que implementa UserDetails.
        return user;
    }
}