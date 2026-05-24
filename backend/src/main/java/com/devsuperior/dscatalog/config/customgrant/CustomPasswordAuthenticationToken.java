package com.devsuperior.dscatalog.config.customgrant;

import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationGrantAuthenticationToken;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

// CustomPasswordAuthenticationToken: Esta classe estende OAuth2AuthorizationGrantAuthenticationToken,
// que é a classe base para tokens de autenticação de concessão OAuth2.
// Ela representa um token de autenticação específico para o fluxo de concessão "password" customizado.
// Este token é criado pelo CustomPasswordAuthenticationConverter e processado pelo
// CustomPasswordAuthenticationProvider.
public class CustomPasswordAuthenticationToken extends OAuth2AuthorizationGrantAuthenticationToken {

	// serialVersionUID: Usado para controle de versão durante a serialização.
	private static final long serialVersionUID = 1L;

	// username: Armazena o nome de usuário fornecido na requisição de token.
	private final String username;
	// password: Armazena a senha fornecida na requisição de token.
	private final String password;
	// scopes: Armazena o conjunto de escopos (permissões) solicitados pelo cliente.
	private final Set<String> scopes;

	// Construtor para CustomPasswordAuthenticationToken.
	// clientPrincipal: A autenticação do cliente OAuth2 (o cliente que está solicitando o token).
	// scopes: Opcional. O conjunto de escopos que o cliente deseja para o token.
	// additionalParameters: Opcional. Um mapa de parâmetros adicionais da requisição.
	public CustomPasswordAuthenticationToken(Authentication clientPrincipal,
											 @Nullable Set<String> scopes, @Nullable Map<String, Object> additionalParameters) {

		// Chama o construtor da classe pai (OAuth2AuthorizationGrantAuthenticationToken).
		// O tipo de concessão é "password".
		super(new AuthorizationGrantType("password"), clientPrincipal, additionalParameters);

		// Extrai o username e password dos parâmetros adicionais.
		// Estes parâmetros são esperados na requisição do tipo "password".
		this.username = (String) additionalParameters.get("username");
		this.password = (String) additionalParameters.get("password");

		// Inicializa o conjunto de escopos. Se 'scopes' for nulo, usa um conjunto vazio.
		// Collections.unmodifiableSet garante que o conjunto de escopos não possa ser modificado após a criação.
		this.scopes = Collections.unmodifiableSet(
				scopes != null ? new HashSet<>(scopes) : Collections.emptySet());
	}

	// getUsername(): Retorna o nome de usuário associado a este token de autenticação.
	public String getUsername() {
		return this.username;
	}

	// getPassword(): Retorna a senha associada a este token de autenticação.
	public String getPassword() {
		return this.password;
	}

	// getScopes(): Retorna o conjunto imutável de escopos solicitados para este token.
	public Set<String> getScopes() {
		return this.scopes;
	}
}