package com.devsuperior.dscatalog.entities;

import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;


import java.util.*;

// @Entity: Define que a classe é uma entidade JPA que será mapeada para uma tabela no banco de dados.
@Entity
// @Table: Define o nome da tabela no banco de dados (tb_user).
@Table(name = "tb_user")
// A classe User implementa UserDetails, uma interface do Spring Security que fornece informações essenciais do usuário
// (como credenciais, autoridades e status da conta) para o framework de segurança.
public class User implements UserDetails {
    private static final long serialVersionUID = 1L;

    // @Id: Define que o campo id é a chave primária da tabela.
    @Id
    // @GeneratedValue: Define a estratégia de geração automática de IDs (Auto-incremento no banco).
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String firstName;
    private String lastName;

    @Column(unique = true)//: Vai ser usado para garantir que o email seja único no banco.
    private String email;
    private String password;

    // @ManyToMany: Define um relacionamento de Muitos para Muitos entre Usuários e Perfis (Roles).
    // fetch = FetchType.EAGER: Indica que os roles devem ser carregados imediatamente junto com o usuário.
    @ManyToMany(fetch = FetchType.EAGER)
    // @JoinTable: Define as configurações da tabela auxiliar de junção (N para N).
    @JoinTable(name = "tb_user_role",
            // joinColumns: Define a chave estrangeira da própria entidade (User) na tabela de junção.
            joinColumns = @JoinColumn(name = "user_id"),
            // inverseJoinColumns: Define a chave estrangeira da outra entidade (Role) na tabela de junção.
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    // Set: Garante que não haverá duplicidade de Perfis (Roles) para o mesmo Usuário.
    private Set<Role> roles = new HashSet<>();

    public User(){
    }

    public User(Long id, String firstName, String lastName, String email, String password) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    // getAuthorities(): Retorna a coleção de GrantedAuthority (perfis/roles) concedidas ao usuário.
    // Essencial para o Spring Security determinar as permissões do usuário.
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles;
    }

    public String getPassword() {
        return password;
    }

    // getUsername(): Retorna o nome de usuário usado para autenticar o usuário.
    // Neste caso, o email é usado como nome de usuário.
    @Override
    public String getUsername() {
        return email;
    }

    // isAccountNonExpired(): Indica se a conta do usuário expirou. Retorna true para não expirada.
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    // isAccountNonLocked(): Indica se o usuário está bloqueado ou desbloqueado. Retorna true para não bloqueado.
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    // isCredentialsNonExpired(): Indica se as credenciais (senha) do usuário expiraram. Retorna true para não expiradas.
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    // isEnabled(): Indica se o usuário está habilitado ou desabilitado. Retorna true para habilitado.
    @Override
    public boolean isEnabled() {
        return true;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    // addRole(): Método auxiliar para adicionar um Role ao conjunto de roles do usuário.
    public void addRole(Role role){
        roles.add(role);
    }

    // hasRoles(): Método auxiliar para verificar se o usuário possui um determinado perfil (role).
    public boolean hasRoles(String roleName){
        for(Role role: roles){
            if(role.getAuthority().equals(roleName)){
                return true;
            }
        }
        return false;
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof User user)) return false;

        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}