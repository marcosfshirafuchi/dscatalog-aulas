package com.devsuperior.dscatalog.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;

/**
 * Classe de configuração para o Servidor de Recursos (Resource Server).
 * Esta classe define as regras de segurança para proteger os endpoints da API,
 * validando tokens JWT e configurando CORS e CSRF.
 */
@Configuration
@EnableWebSecurity // Habilita a segurança web no Spring.
@EnableMethodSecurity // Habilita a segurança baseada em anotações de método (ex: @PreAuthorize).
public class ResourceServerConfig {

	// Injeta as origens permitidas para CORS do arquivo application.properties.
	@Value("${cors.origins}")
	private String corsOrigins;

	/**
	 * Título: Liberar H2 Console no modo teste
	 *
	 * Configura uma cadeia de filtros de segurança específica para o console H2.
	 * Esta configuração é ativada apenas quando o perfil "test" está ativo.
	 *
	 * @param http Objeto HttpSecurity para configurar a segurança web.
	 * @return Uma SecurityFilterChain configurada para o H2 Console.
	 * @throws Exception Se houver um erro na configuração.
	 */
	@Bean
	@Profile("test") // Ativa este bean apenas quando o perfil "test" estiver ativo.
	@Order(1) // Define uma ordem de precedência alta para este filtro.
	public SecurityFilterChain h2SecurityFilterChain(HttpSecurity http) throws Exception {
		// Configura o filtro para corresponder apenas às requisições para o H2 Console.
		http.securityMatcher(PathRequest.toH2Console())
				// Desabilita a proteção CSRF para o H2 Console, pois ele não é vulnerável a ataques CSRF.
				.csrf(csrf -> csrf.disable())
				// Desabilita as opções de frame para permitir que o H2 Console seja exibido em um frame (necessário para a UI).
				.headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable()));
		return http.build();
	}

	/**
	 * Título: Configurar controle de acesso aos recursos, Configurar CSRF, CORS, Configurar token
	 *
	 * Configura a cadeia de filtros de segurança principal para o Resource Server.
	 * Esta configuração lida com a proteção dos endpoints da API, validação de JWTs,
	 * CSRF e CORS.
	 *
	 * @param http Objeto HttpSecurity para configurar a segurança web.
	 * @return Uma SecurityFilterChain configurada para o Resource Server.
	 * @throws Exception Se houver um erro na configuração.
	 */
	@Bean
	@Order(3) // Define a ordem deste filtro na cadeia de filtros de segurança.
	public SecurityFilterChain rsSecurityFilterChain(HttpSecurity http) throws Exception {

		// Tópico: Configurar CSRF
		// Desabilita a proteção CSRF (Cross-Site Request Forgery).
		// Em APIs RESTful sem sessões baseadas em cookies, CSRF geralmente não é necessário,
		// pois a autenticação é feita via tokens no cabeçalho.
		http.csrf(csrf -> csrf.disable());

		// Tópico: Configurar controle de acesso aos recursos
		// Configura as regras de autorização para as requisições HTTP.
		http.authorizeHttpRequests(authorize ->
				// Esta configuração permite que TODAS as requisições (anyRequest())
				// acessem os recursos SEM NENHUMA AUTENTICAÇÃO ou AUTORIZAÇÃO.
				// Ou seja, qualquer um pode acessar qualquer endpoint sem precisar de token.
				// Se a intenção é proteger os endpoints e usar @PreAuthorize,
				// esta linha deveria ser 'authorize.anyRequest().authenticated()'.
				authorize.anyRequest().permitAll()
		);

		// Tópico: Configurar token
		// Configura o Resource Server para usar tokens JWT.
		// O Spring Security irá validar os tokens JWT recebidos nas requisições.
		http.oauth2ResourceServer(oauth2ResourceServer ->
				oauth2ResourceServer.jwt(Customizer.withDefaults())
		);

		// Tópico: Configurar CORS
		// Habilita a configuração CORS (Cross-Origin Resource Sharing) definida no método corsConfigurationSource().
		http.cors(cors -> cors.configurationSource(corsConfigurationSource()));

		return http.build();
	}

	/**
	 * Título: Configurar token
	 *
	 * Configura o conversor de JWT para extrair as autoridades (papéis) do token.
	 * Isso é crucial para que o Spring Security possa aplicar as regras de autorização
	 * baseadas nos papéis contidos no JWT.
	 *
	 * @return Uma instância de JwtAuthenticationConverter.
	 */
	@Bean
	public JwtAuthenticationConverter jwtAuthenticationConverter() {
		// Cria um conversor para extrair GrantedAuthorities do JWT.
		JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
		// Define o nome da claim no JWT que contém as autoridades (ex: "authorities").
		grantedAuthoritiesConverter.setAuthoritiesClaimName("authorities");
		// Remove o prefixo padrão "SCOPE_" que o Spring Security adiciona,
		// permitindo que as autoridades sejam usadas diretamente (ex: "ROLE_OPERATOR").
		grantedAuthoritiesConverter.setAuthorityPrefix("");

		// Cria o conversor principal de JWT.
		JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
		// Associa o conversor de GrantedAuthorities ao conversor de JWT.
		jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
		return jwtAuthenticationConverter;
	}

	/**
	 * Título: Configurar CSRF, CORS
	 *
	 * Configura as políticas de CORS (Cross-Origin Resource Sharing).
	 * Define quais origens, métodos HTTP e cabeçalhos são permitidos para requisições cross-origin.
	 *
	 * @return Uma instância de CorsConfigurationSource.
	 */
	@Bean
	CorsConfigurationSource corsConfigurationSource() {
		// Divide a string de origens permitidas (configurada em application.properties) em um array.
		String[] origins = corsOrigins.split(",");

		// Cria uma nova configuração CORS.
		CorsConfiguration corsConfig = new CorsConfiguration();
		// Define os padrões de origem permitidos.
		corsConfig.setAllowedOriginPatterns(Arrays.asList(origins));
		// Define os métodos HTTP permitidos.
		corsConfig.setAllowedMethods(Arrays.asList("POST", "GET", "PUT", "DELETE", "PATCH"));
		// Permite o envio de credenciais (cookies, cabeçalhos de autorização).
		corsConfig.setAllowCredentials(true);
		// Define os cabeçalhos permitidos.
		corsConfig.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));

		// Registra a configuração CORS para todos os caminhos ("/**").
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", corsConfig);
		return source;
	}

	/**
	 * Título: Configurar CSRF, CORS
	 *
	 * Registra o filtro CORS no Spring Security.
	 * Garante que o filtro CORS seja executado com a mais alta precedência.
	 *
	 * @return Um FilterRegistrationBean para o CorsFilter.
	 */
	@Bean
	FilterRegistrationBean<CorsFilter> corsFilter() {
		FilterRegistrationBean<CorsFilter> bean = new FilterRegistrationBean<>(
				new CorsFilter(corsConfigurationSource()));
		// Define a ordem do filtro CORS para ser o mais alto, garantindo que ele seja executado primeiro.
		bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
		return bean;
	}
}
