package com.devsuperior.dscatalog.services;

import com.devsuperior.dscatalog.dto.RoleDTO;
import com.devsuperior.dscatalog.dto.UserDTO;
import com.devsuperior.dscatalog.dto.UserInsertDTO;
import com.devsuperior.dscatalog.dto.UserUpdateDTO;
import com.devsuperior.dscatalog.entities.Role;
import com.devsuperior.dscatalog.entities.User;
import com.devsuperior.dscatalog.projections.UserDetailsProjection;
import com.devsuperior.dscatalog.services.exceptions.DatabaseException;
import com.devsuperior.dscatalog.services.exceptions.ResourceNotFoundException;
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
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service responsável pelas regras de negócio relacionadas aos usuários.
 *
 * Além das operações CRUD (Create, Read, Update e Delete),
 * esta classe também implementa UserDetailsService,
 * que é a interface utilizada pelo Spring Security
 * para localizar usuários durante o processo de autenticação.
 *
 * Fluxo de autenticação:
 *
 * Login → UserDetailsService → Banco de Dados
 *       → Usuário encontrado
 *       → Senha validada
 *       → Token gerado
 */
@Service
public class UserService implements UserDetailsService {


    /**
     * Responsável por criptografar senhas antes de serem
     * armazenadas no banco de dados.
     *
     * Exemplo:
     *
     * Senha digitada:
     *
     * 123456
     *
     * Senha armazenada:
     *
     * $2a$10$TjN3K....
     *
     * Isso aumenta significativamente a segurança da aplicação.
     *
     * O Spring Security posteriormente compara a senha digitada
     * com o hash armazenado utilizando o método matches().
     */
    @Autowired
    private PasswordEncoder passwordEncoder;

    // @Autowired: Injeta uma instância de UserRepository para realizar operações de
    // persistência (CRUD) na entidade User.
    @Autowired
    private UserRepository repository;

    // @Autowired: Injeta uma instância de RoleRepository para buscar e manipular roles.
    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private AuthService authService;

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
    @Transactional(readOnly = true)
    public UserDTO findMe() {
        User entity = authService.authenticated();
        return new UserDTO(entity);
    }

    @Transactional(readOnly = true)
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

        /**
         * Todo usuário criado através deste endpoint
         * recebe automaticamente o perfil ROLE_OPERATOR.
         *
         * Isso garante que o usuário tenha pelo menos
         * uma permissão básica dentro do sistema.
         *
         * Exemplo:
         *
         * ADMIN → criado manualmente
         * OPERATOR → criado pelo cadastro padrão
         */
        entity.getRoles().clear();

        Role role = roleRepository.findByAuthority("ROLE_OPERATOR");

        entity.getRoles().add(role);

        /**
         * A senha nunca deve ser salva em texto puro.
         *
         * Antes de persistir o usuário, a senha é criptografada
         * utilizando o algoritmo configurado no PasswordEncoder.
         *
         * Exemplo:
         *
         * Entrada:
         * senha123
         *
         * Saída:
         * $2a$10$Z3R....
         */
        entity.setPassword(passwordEncoder.encode(dto.getPassword()));
        // Salva a nova entidade User no banco de dados.
        entity = repository.save(entity);
        // Converte a entidade salva para UserDTO e a retorna.
        return new UserDTO(entity);
    }

    // @Transactional: Indica que o método é transacional.
    public UserDTO update(Long id, UserUpdateDTO dto) {
        try {
            /**
             * getReferenceById() retorna um proxy gerenciado pelo JPA.
             *
             * Diferente do findById(), ele não realiza imediatamente
             * uma consulta ao banco.
             *
             * A consulta só será executada quando algum atributo
             * da entidade for acessado.
             *
             * Essa abordagem costuma ser mais eficiente em operações
             * de atualização.
             */
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
        /**
         * Verificação preventiva.
         *
         * Caso o registro não exista,
         * evitamos executar um DELETE desnecessário
         * e retornamos uma mensagem mais amigável
         * para a API.
         */
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

        /**
         * Remove todas as roles atuais do usuário.
         *
         * Isso é importante principalmente durante
         * operações de atualização.
         *
         * Após a limpeza, os perfis recebidos pelo DTO
         * serão adicionados novamente.
         */
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

    /**
     * Método obrigatório da interface UserDetailsService.
     *
     * O Spring Security chama este método automaticamente
     * durante o processo de autenticação.
     *
     * Exemplo:
     *
     * POST /oauth2/token
     *
     * username = maria@gmail.com
     * password = 123456
     *
     * O Spring chama:
     *
     * loadUserByUsername("maria@gmail.com")
     *
     * Este método retorna:
     *
     * - email
     * - senha criptografada
     * - roles/perfis
     *
     * que serão utilizados para autenticar o usuário.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Busca o usuário e seus roles pelo email (username) usando uma projeção personalizada.
        List<UserDetailsProjection> result = repository.searchUserAndRolesByEmail(username);
        /**
         * Caso nenhum registro seja encontrado,
         * o Spring Security interrompe o processo
         * de autenticação e retorna erro.
         */
        if (result.size() == 0) {
            throw new UsernameNotFoundException("Email not found");
        }
        /**
         * Reconstrói o objeto User utilizando os dados
         * retornados pela projeção.
         *
         * Como a consulta retorna múltiplas linhas
         * (uma para cada perfil),
         * precisamos montar manualmente o usuário
         * e adicionar todas as roles encontradas.
         */
        User user = new User();
        user.setEmail(username);
        user.setPassword(result.get(0).getPassword()); // A senha é a mesma para todas as projeções do mesmo usuário.
        /**
         * Adiciona cada perfil encontrado ao usuário.
         *
         * Exemplo:
         *
         * ROLE_OPERATOR
         * ROLE_ADMIN
         *
         * Essas roles serão utilizadas posteriormente
         * pelo Spring Security para autorização.
         *
         * Exemplos:
         *
         * hasRole("ADMIN")
         * hasRole("OPERATOR")
         */
        for (UserDetailsProjection projection : result) {
            user.addRole(
                    new Role(
                            projection.getRoleId(),
                            projection.getAuthority()
                    )
            );
        }
        // Retorna a entidade User, que implementa UserDetails.
        return user;
    }
}