package com.devsuperior.dscatalog.config;

import com.devsuperior.dscatalog.config.customgrant.CustomPasswordAuthenticationConverter;
import com.devsuperior.dscatalog.config.customgrant.CustomPasswordAuthenticationProvider;
import com.devsuperior.dscatalog.config.customgrant.CustomUserAuthorities;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.authorization.InMemoryOAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.InMemoryOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.oauth2.server.authorization.token.*;
import org.springframework.security.web.SecurityFilterChain;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

/**
 * Classe de configuração para o Servidor de Autorização OAuth2.
 * Esta classe configura os beans necessários para o funcionamento do Spring Authorization Server,
 * incluindo a cadeia de filtros de segurança, gerenciamento de clientes, geração de tokens JWT,
 * e um fluxo de concessão de senha customizado.
 */
@Configuration
public class AuthorizationServerConfig {

	// Injeta o ID do cliente configurado no application.properties
	@Value("${security.client-id}")
	private String clientId;

	// Injeta o segredo do cliente configurado no application.properties
	@Value("${security.client-secret}")
	private String clientSecret;

	// Injeta a duração de validade do JWT em segundos configurada no application.properties
	@Value("${security.jwt.duration}")
	private Integer jwtDurationSeconds;

	@Autowired
	private PasswordEncoder passwordEncoder;

	// Injeta o serviço UserDetailsService para carregar detalhes do usuário
	@Autowired
	private UserDetailsService userDetailsService;

	/**
	 * Título: Habilitar Authorization server
	 *
	 * Configura a cadeia de filtros de segurança para o Servidor de Autorização.
	 * Esta é a principal configuração para o endpoint de token OAuth2,
	 * aplicando as configurações padrão do Spring Authorization Server.
	 *
	 * @param http Objeto HttpSecurity para configurar a segurança web.
	 * @return Uma SecurityFilterChain configurada.
	 * @throws Exception Se houver um erro na configuração.
	 */
	@Bean
	@Order(2) // Define a ordem deste filtro na cadeia de filtros de segurança.
	public SecurityFilterChain asSecurityFilterChain(HttpSecurity http) throws Exception {

		// Aplica as configurações padrão do Spring Authorization Server.
		OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);

		// @formatter:off
		// Título: Configurar autenticação / password encoder
		// Configura o endpoint de token para usar um fluxo de concessão de senha customizado.
		http.getConfigurer(OAuth2AuthorizationServerConfigurer.class)
				.tokenEndpoint(tokenEndpoint -> tokenEndpoint
						// Define um conversor para transformar a requisição de token em um objeto de autenticação customizado.
						.accessTokenRequestConverter(new CustomPasswordAuthenticationConverter())
						// Define um provedor de autenticação customizado para lidar com o fluxo de concessão de senha.
						// Este provedor utiliza o UserDetailsService e o PasswordEncoder configurados.
						.authenticationProvider(new CustomPasswordAuthenticationProvider(authorizationService(), tokenGenerator(), userDetailsService, passwordEncoder)));

		// Configura o servidor de recursos OAuth2 para validar tokens JWT.
		// Isso permite que a mesma aplicação atue como Servidor de Autorização e Servidor de Recursos.
		http.oauth2ResourceServer(oauth2ResourceServer -> oauth2ResourceServer.jwt(Customizer.withDefaults()));
		// @formatter:on

		return http.build();
	}

	/**
	 * Título: Habilitar Authorization server
	 *
	 * Bean para o serviço de autorização OAuth2.
	 * Responsável por armazenar e recuperar autorizações OAuth2 (por exemplo, códigos de autorização, tokens de acesso).
	 * Usamos uma implementação em memória para fins de demonstração.
	 *
	 * @return Uma instância de InMemoryOAuth2AuthorizationService.
	 */
	@Bean
	public OAuth2AuthorizationService authorizationService() {
		return new InMemoryOAuth2AuthorizationService();
	}

	/**
	 * Título: Habilitar Authorization server
	 *
	 * Bean para o serviço de consentimento de autorização OAuth2.
	 * Responsável por armazenar e recuperar consentimentos do usuário para acesso a recursos.
	 * Usamos uma implementação em memória para fins de demonstração.
	 *
	 * @return Uma instância de InMemoryOAuth2AuthorizationConsentService.
	 */
	@Bean
	public OAuth2AuthorizationConsentService oAuth2AuthorizationConsentService() {
		return new InMemoryOAuth2AuthorizationConsentService();
	}


	/**
	 * Título: Registrar aplicação cliente
	 *
	 * Bean para o repositório de clientes registrados.
	 * Define os clientes que podem se autenticar com este servidor de autorização.
	 * Usamos uma implementação em memória para fins de demonstração.
	 *
	 * @return Uma instância de InMemoryRegisteredClientRepository contendo o cliente configurado.
	 */
	@Bean
	public RegisteredClientRepository registeredClientRepository() {
		// @formatter:off
		// Cria um cliente registrado com ID, segredo, escopos e tipos de concessão.
		RegisteredClient registeredClient = RegisteredClient
				.withId(UUID.randomUUID().toString()) // ID único para o cliente
				.clientId(clientId) // ID do cliente (do application.properties)
				.clientSecret(passwordEncoder.encode(clientSecret)) // Segredo do cliente (codificado)
				.scope("read") // Escopo de permissão "read"
				.scope("write") // Escopo de permissão "write"
				.authorizationGrantType(new AuthorizationGrantType("password")) // Tipo de concessão de senha customizado
				.tokenSettings(tokenSettings()) // Configurações de token para este cliente
				.clientSettings(clientSettings()) // Configurações gerais do cliente
				.build();
		// @formatter:on

		return new InMemoryRegisteredClientRepository(registeredClient);
	}

	/**
	 * Título: Configuration token (codificação, formato, assinatura)
	 *
	 * Bean para as configurações de token.
	 * Define propriedades como o formato do token de acesso e seu tempo de vida.
	 *
	 * @return Uma instância de TokenSettings.
	 */
	@Bean
	public TokenSettings tokenSettings() {
		// @formatter:off
		return TokenSettings.builder()
				// Define o formato do token de acesso como "SELF_CONTAINED" (JWT).
				.accessTokenFormat(OAuth2TokenFormat.SELF_CONTAINED)
				// Define o tempo de vida do token de acesso com base na propriedade configurada.
				.accessTokenTimeToLive(Duration.ofSeconds(jwtDurationSeconds))
				.build();
		// @formatter:on
	}

	/**
	 * Título: Registrar aplicação cliente
	 *
	 * Bean para as configurações do cliente.
	 * Atualmente, retorna as configurações padrão.
	 *
	 * @return Uma instância de ClientSettings.
	 */
	@Bean
	public ClientSettings clientSettings() {
		return ClientSettings.builder().build();
	}

	/**
	 * Título: Habilitar Authorization server
	 *
	 * Bean para as configurações do servidor de autorização.
	 * Atualmente, retorna as configurações padrão.
	 *
	 * @return Uma instância de AuthorizationServerSettings.
	 */
	@Bean
	public AuthorizationServerSettings authorizationServerSettings() {
		return AuthorizationServerSettings.builder().build();
	}

	/**
	 * Título: Configuration token (codificação, formato, assinatura)
	 *
	 * Bean para o gerador de tokens OAuth2.
	 * Configura como os tokens de acesso são gerados, incluindo a geração de JWTs.
	 *
	 * @return Uma instância de OAuth2TokenGenerator.
	 */
	@Bean
	public OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator() {
		// Cria um codificador JWT usando a fonte de chaves JWK.
		NimbusJwtEncoder jwtEncoder = new NimbusJwtEncoder(jwkSource());
		// Cria um gerador JWT.
		JwtGenerator jwtGenerator = new JwtGenerator(jwtEncoder);
		// Aplica um customizador para adicionar claims personalizadas ao JWT.
		jwtGenerator.setJwtCustomizer(tokenCustomizer());
		// Cria um gerador de token de acesso padrão.
		OAuth2AccessTokenGenerator accessTokenGenerator = new OAuth2AccessTokenGenerator();
		// Retorna um gerador delegado que pode usar múltiplos geradores (JWT e Access Token padrão).
		return new DelegatingOAuth2TokenGenerator(jwtGenerator, accessTokenGenerator);
	}

	/**
	 * Título: Configuration token (codificação, formato, assinatura)
	 *
	 * Bean para o customizador de tokens JWT.
	 * Adiciona claims personalizadas (autoridades e nome de usuário) ao JWT.
	 *
	 * @return Uma instância de OAuth2TokenCustomizer.
	 */
	@Bean
	public OAuth2TokenCustomizer<JwtEncodingContext> tokenCustomizer() {
		return context -> {
			// Obtém o principal de autenticação do cliente.
			OAuth2ClientAuthenticationToken principal = context.getPrincipal();
			// Converte o principal para o tipo CustomUserAuthorities (do fluxo customizado).
			CustomUserAuthorities user = (CustomUserAuthorities) principal.getDetails();
			// Extrai as autoridades do usuário e as mapeia para uma lista de strings.
			List<String> authorities = user.getAuthorities().stream().map(x -> x.getAuthority()).toList();
			// Verifica se o token sendo gerado é um token de acesso.
			if (context.getTokenType().getValue().equals("access_token")) {
				// @formatter:off
				// Adiciona as autoridades e o nome de usuário como claims ao JWT.
				context.getClaims()
						.claim("authorities", authorities) // Claim "authorities" com a lista de papéis
						.claim("username", user.getUsername()); // Claim "username" com o nome de usuário
				// @formatter:on
			}
		};
	}

	/**
	 * Título: Configuration token (codificação, formato, assinatura)
	 *
	 * Bean para o decodificador JWT.
	 * Usado para decodificar e validar tokens JWT.
	 *
	 * @param jwkSource A fonte de chaves JWK para validação.
	 * @return Uma instância de JwtDecoder.
	 */
	@Bean
	public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
		return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
	}

	/**
	 * Título: Configuration token (codificação, formato, assinatura)
	 *
	 * Bean para a fonte de chaves JWK (JSON Web Key).
	 * Fornece as chaves públicas e privadas RSA usadas para assinar e verificar JWTs.
	 *
	 * @return Uma instância de JWKSource.
	 */
	@Bean
	public JWKSource<SecurityContext> jwkSource() {
		// Gera um par de chaves RSA.
		RSAKey rsaKey = generateRsa();
		// Cria um JWKSet com a chave RSA gerada.
		JWKSet jwkSet = new JWKSet(rsaKey);
		// Retorna uma JWKSource que seleciona chaves do JWKSet.
		return (jwkSelector, securityContext) -> jwkSelector.select(jwkSet);
	}

	/**
	 * Título: Configuration token (codificação, formato, assinatura)
	 *
	 * Método auxiliar para gerar uma chave RSA.
	 *
	 * @return Uma instância de RSAKey.
	 */
	private static RSAKey generateRsa() {
		// Gera um par de chaves RSA (pública e privada).
		KeyPair keyPair = generateRsaKey();
		// Extrai a chave pública.
		RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
		// Extrai a chave privada.
		RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
		// Constrói e retorna uma RSAKey com as chaves e um ID único.
		return new RSAKey.Builder(publicKey).privateKey(privateKey).keyID(UUID.randomUUID().toString()).build();
	}

	/**
	 * Título: Configuration token (codificação, formato, assinatura)
	 *
	 * Método auxiliar para gerar um par de chaves RSA.
	 *
	 * @return Um KeyPair contendo as chaves pública e privada.
	 */
	private static KeyPair generateRsaKey() {
		KeyPair keyPair;
		try {
			// Obtém um gerador de pares de chaves para RSA.
			KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
			// Inicializa o gerador com um tamanho de chave de 2048 bits.
			keyPairGenerator.initialize(2048);
			// Gera o par de chaves.
			keyPair = keyPairGenerator.generateKeyPair();
		} catch (Exception ex) {
			// Lança uma exceção em caso de erro na geração das chaves.
			throw new IllegalStateException(ex);
		}
		return keyPair;
	}
}