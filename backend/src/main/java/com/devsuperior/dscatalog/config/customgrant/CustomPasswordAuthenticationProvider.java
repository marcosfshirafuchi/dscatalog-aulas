package com.devsuperior.dscatalog.config.customgrant;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AccessTokenAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.context.AuthorizationServerContextHolder;
import org.springframework.security.oauth2.server.authorization.token.DefaultOAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.util.Assert;

import java.security.Principal;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

// CustomPasswordAuthenticationProvider: Esta classe é um provedor de autenticação customizado
// para o fluxo de concessão "password" do OAuth2. Ela é responsável por validar as credenciais
// do usuário (username/password) e, se válidas, emitir um token de acesso.
// Implementa AuthenticationProvider, que é a interface central para provedores de autenticação no Spring Security.
public class CustomPasswordAuthenticationProvider implements AuthenticationProvider {

	// Constante para a URI de erro, conforme especificação OAuth2.
	private static final String ERROR_URI = "https://datatracker.ietf.org/doc/html/rfc6749#section-5.2";

	// Serviço para gerenciar autorizações OAuth2 (salvar e recuperar).
	private final OAuth2AuthorizationService authorizationService;

	// Serviço para carregar detalhes do usuário (username, password, authorities).
	private final UserDetailsService userDetailsService;

	// Gerador de tokens OAuth2 (access tokens, refresh tokens).
	private final OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator;

	// Codificador de senhas para verificar a senha fornecida com a senha armazenada.
	private final PasswordEncoder passwordEncoder;

	// Variáveis temporárias para armazenar username e password durante o processo de autenticação.
	private String username = "";
	private String password = "";

	// Conjunto de escopos autorizados para o token de acesso.
	private Set<String> authorizedScopes = new HashSet<>();

	// Construtor do provedor de autenticação. Recebe as dependências necessárias.
	public CustomPasswordAuthenticationProvider(OAuth2AuthorizationService authorizationService,
												OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator,
												UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {

		// Asserts para garantir que as dependências não são nulas.
		Assert.notNull(authorizationService, "authorizationService cannot be null");
		Assert.notNull(tokenGenerator, "TokenGenerator cannot be null");
		Assert.notNull(userDetailsService, "UserDetailsService cannot be null");
		Assert.notNull(passwordEncoder, "PasswordEncoder cannot be null");

		// Atribui as dependências aos campos da classe.
		this.authorizationService = authorizationService;
		this.tokenGenerator = tokenGenerator;
		this.userDetailsService = userDetailsService;
		this.passwordEncoder = passwordEncoder;
	}

	// authenticate: Método principal onde a lógica de autenticação ocorre.
	// Recebe um objeto Authentication (neste caso, CustomPasswordAuthenticationToken)
	// e retorna um objeto Authentication autenticado se as credenciais forem válidas.
	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {

		// Converte o objeto Authentication genérico para o tipo específico esperado.
		CustomPasswordAuthenticationToken customPasswordAuthenticationToken = (CustomPasswordAuthenticationToken) authentication;

		// Autentica o cliente OAuth2 (não o usuário final) e verifica se ele é válido.
		// Se o cliente não for autenticado ou for inválido, uma exceção é lançada.
		OAuth2ClientAuthenticationToken clientPrincipal = getAuthenticatedClientElseThrowInvalidClient(customPasswordAuthenticationToken);

		// Obtém o cliente registrado associado ao principal do cliente.
		RegisteredClient registeredClient = clientPrincipal.getRegisteredClient();

		// Extrai o username e password do token de autenticação customizado.
		username = customPasswordAuthenticationToken.getUsername();
		password = customPasswordAuthenticationToken.getPassword();

		UserDetails user = null;
		try {
			// Carrega os detalhes do usuário usando o UserDetailsService.
			user = userDetailsService.loadUserByUsername(username);
		} catch (UsernameNotFoundException e) {
			// Se o usuário não for encontrado, lança uma exceção de autenticação.
			throw new OAuth2AuthenticationException("Invalid credentials");
		}

		// Verifica se a senha fornecida corresponde à senha armazenada (codificada)
		// e se o username carregado corresponde ao username fornecido.
		if (!passwordEncoder.matches(password, user.getPassword()) || !user.getUsername().equals(username)) {
			throw new OAuth2AuthenticationException("Invalid credentials");
		}

		// Determina os escopos autorizados para o token de acesso.
		// Filtra os escopos que o usuário possui e que o cliente registrado solicitou.
		authorizedScopes = user.getAuthorities().stream()
				.map(scope -> scope.getAuthority())
				.filter(scope -> registeredClient.getScopes().contains(scope))
				.collect(Collectors.toSet());

		//-----------Cria um novo Contexto de Segurança----------
		// Obtém o token de autenticação do cliente do contexto de segurança atual.
		OAuth2ClientAuthenticationToken oAuth2ClientAuthenticationToken = (OAuth2ClientAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

		// Cria um objeto CustomUserAuthorities com o username e as autoridades do usuário.
		CustomUserAuthorities customPasswordUser = new CustomUserAuthorities(username, user.getAuthorities());

		// Define os detalhes do token de autenticação do cliente com as autoridades do usuário.
		oAuth2ClientAuthenticationToken.setDetails(customPasswordUser);

		// Cria um novo contexto de segurança vazio e define o token de autenticação do cliente nele.
		var newcontext = SecurityContextHolder.createEmptyContext();
		newcontext.setAuthentication(oAuth2ClientAuthenticationToken);
		SecurityContextHolder.setContext(newcontext);

		//-----------BUILDERS DE TOKEN----------
		// Constrói o contexto do token, que contém todas as informações necessárias para gerar um token.
		DefaultOAuth2TokenContext.Builder tokenContextBuilder = DefaultOAuth2TokenContext.builder()
				.registeredClient(registeredClient) // Cliente OAuth2 registrado.
				.principal(clientPrincipal) // Principal do cliente autenticado.
				.authorizationServerContext(AuthorizationServerContextHolder.getContext()) // Contexto do servidor de autorização.
				.authorizedScopes(authorizedScopes) // Escopos autorizados.
				.authorizationGrantType(new AuthorizationGrantType("password")) // Tipo de concessão (password).
				.authorizationGrant(customPasswordAuthenticationToken); // O token de autenticação customizado.

		// Constrói o objeto de autorização OAuth2, que armazena o estado da autorização.
		OAuth2Authorization.Builder authorizationBuilder = OAuth2Authorization.withRegisteredClient(registeredClient)
				.attribute(Principal.class.getName(), clientPrincipal) // Atributo do principal.
				.principalName(clientPrincipal.getName()) // Nome do principal.
				.authorizationGrantType(new AuthorizationGrantType("password")) // Tipo de concessão.
				.authorizedScopes(authorizedScopes); // Escopos autorizados.

		//-----------ACCESS TOKEN----------
		// Constrói o contexto do token para o Access Token.
		OAuth2TokenContext tokenContext = tokenContextBuilder.tokenType(OAuth2TokenType.ACCESS_TOKEN).build();

		// Gera o Access Token usando o gerador de tokens.
		OAuth2Token generatedAccessToken = this.tokenGenerator.generate(tokenContext);

		// Verifica se o Access Token foi gerado com sucesso.
		if (generatedAccessToken == null) {
			OAuth2Error error = new OAuth2Error(OAuth2ErrorCodes.SERVER_ERROR,
					"The token generator failed to generate the access token.", ERROR_URI);
			throw new OAuth2AuthenticationException(error);
		}

		// Cria um objeto OAuth2AccessToken a partir do token gerado.
		OAuth2AccessToken accessToken = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER,
				generatedAccessToken.getTokenValue(), generatedAccessToken.getIssuedAt(),
				generatedAccessToken.getExpiresAt(), tokenContext.getAuthorizedScopes());

		// Se o token gerado for um ClaimAccessor (contém claims), adiciona os claims à autorização.
		if (generatedAccessToken instanceof ClaimAccessor) {
			authorizationBuilder.token(accessToken, (metadata) ->
					metadata.put(OAuth2Authorization.Token.CLAIMS_METADATA_NAME, ((ClaimAccessor) generatedAccessToken).getClaims()));
		} else {
			// Caso contrário, apenas adiciona o Access Token à autorização.
			authorizationBuilder.accessToken(accessToken);
		}

		// Constrói o objeto OAuth2Authorization final.
		OAuth2Authorization authorization = authorizationBuilder.build();

		// Salva a autorização no serviço de autorização.
		this.authorizationService.save(authorization);

		// Retorna um OAuth2AccessTokenAuthenticationToken, que representa a autenticação bem-sucedida
		// e contém o Access Token emitido.
		return new OAuth2AccessTokenAuthenticationToken(registeredClient, clientPrincipal, accessToken);
	}

	// supports: Indica se este provedor de autenticação suporta o tipo de token de autenticação fornecido.
	// Retorna true se o tipo de autenticação for CustomPasswordAuthenticationToken ou uma subclasse dele.
	@Override
	public boolean supports(Class<?> authentication) {
		return CustomPasswordAuthenticationToken.class.isAssignableFrom(authentication);
	}

	// getAuthenticatedClientElseThrowInvalidClient: Método auxiliar para extrair e validar
	// o principal do cliente OAuth2 da autenticação.
	private static OAuth2ClientAuthenticationToken getAuthenticatedClientElseThrowInvalidClient(Authentication authentication) {

		OAuth2ClientAuthenticationToken clientPrincipal = null;
		// Verifica se o principal da autenticação é um OAuth2ClientAuthenticationToken.
		if (OAuth2ClientAuthenticationToken.class.isAssignableFrom(authentication.getPrincipal().getClass())) {
			clientPrincipal = (OAuth2ClientAuthenticationToken) authentication.getPrincipal();
		}
		// Se o principal do cliente não for nulo e estiver autenticado, retorna-o.
		if (clientPrincipal != null && clientPrincipal.isAuthenticated()) {
			return clientPrincipal;
		}
		// Caso contrário, lança uma exceção indicando que o cliente é inválido.
		throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_CLIENT);
	}
}