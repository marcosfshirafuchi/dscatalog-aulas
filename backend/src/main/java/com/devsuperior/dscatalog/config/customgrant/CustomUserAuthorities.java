package com.devsuperior.dscatalog.config.customgrant;

import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

// CustomUserAuthorities: Esta classe é um DTO (Data Transfer Object) simples
// usado para encapsular o nome de usuário e as autoridades (roles/permissões)
// de um usuário autenticado no contexto de um fluxo de concessão customizado.
// Ela serve para transferir essas informações entre componentes do Spring Security,
// especialmente no CustomPasswordAuthenticationProvider, para associar as autoridades
// corretas ao principal autenticado.
public class CustomUserAuthorities {

	// username: Armazena o nome de usuário (geralmente o email) do usuário autenticado.
	private String username;

	// authorities: Armazena uma coleção de GrantedAuthority, que representa
	// os papéis ou permissões concedidas ao usuário.
	private Collection<? extends GrantedAuthority> authorities;

	// Construtor: Inicializa um novo objeto CustomUserAuthorities com o nome de usuário
	// e a coleção de autoridades fornecidos.
	public CustomUserAuthorities(String username, Collection<? extends GrantedAuthority> authorities) {
		this.username = username;
		this.authorities = authorities;
	}

	// getUsername(): Retorna o nome de usuário.
	public String getUsername() {
		return username;
	}

	// getAuthorities(): Retorna a coleção de autoridades (roles/permissões) do usuário.
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return authorities;
	}
}