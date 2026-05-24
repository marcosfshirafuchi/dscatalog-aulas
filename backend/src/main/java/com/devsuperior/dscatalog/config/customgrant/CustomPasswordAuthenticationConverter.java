package com.devsuperior.dscatalog.config.customgrant;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import java.util.*;

// CustomPasswordAuthenticationConverter: Esta classe atua como um conversor de requisições HTTP
// para um objeto de autenticação específico (CustomPasswordAuthenticationToken).
// Ela é parte da implementação de um fluxo de concessão de "password" customizado para OAuth2,
// permitindo que o servidor de autorização processe credenciais de usuário (username/password)
// diretamente da requisição para emitir tokens.
public class CustomPasswordAuthenticationConverter implements AuthenticationConverter {

	// convert: Este método é chamado pelo Spring Security para tentar converter uma requisição HTTP
	// em um objeto Authentication. Se a requisição corresponder ao tipo de concessão "password"
	// que este conversor lida, ele extrai os parâmetros necessários e constrói um token.
	@Nullable
	@Override
	public Authentication convert(HttpServletRequest request) {

		// Extrai o tipo de concessão (grant_type) da requisição.
		String grantType = request.getParameter(OAuth2ParameterNames.GRANT_TYPE);

		// Se o grant_type não for "password", este conversor não se aplica, então retorna null.
		// Isso permite que outros conversores na cadeia tentem processar a requisição.
		if (!"password".equals(grantType)) {
			return null;
		}

		// Converte os parâmetros da requisição HTTP para um MultiValueMap para facilitar o acesso.
		MultiValueMap<String, String> parameters = getParameters(request);

		// --- Validação e extração do parâmetro 'scope' (OPCIONAL) ---
		String scope = parameters.getFirst(OAuth2ParameterNames.SCOPE);
		// Verifica se o parâmetro 'scope' está presente e se foi fornecido mais de uma vez,
		// o que seria uma requisição inválida.
		if (StringUtils.hasText(scope) &&
				parameters.get(OAuth2ParameterNames.SCOPE).size() != 1) {
			// Lança uma exceção OAuth2AuthenticationException com o código de erro INVALID_REQUEST.
			throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_REQUEST);
		}

		// --- Validação e extração do parâmetro 'username' (OBRIGATÓRIO) ---
		String username = parameters.getFirst(OAuth2ParameterNames.USERNAME);
		// Verifica se o parâmetro 'username' está ausente ou foi fornecido mais de uma vez.
		if (!StringUtils.hasText(username) ||
				parameters.get(OAuth2ParameterNames.USERNAME).size() != 1) {
			throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_REQUEST);
		}

		// --- Validação e extração do parâmetro 'password' (OBRIGATÓRIO) ---
		String password = parameters.getFirst(OAuth2ParameterNames.PASSWORD);
		// Verifica se o parâmetro 'password' está ausente ou foi fornecido mais de uma vez.
		if (!StringUtils.hasText(password) ||
				parameters.get(OAuth2ParameterNames.PASSWORD).size() != 1) {
			throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_REQUEST);
		}

		// Inicializa um conjunto para os escopos solicitados.
		Set<String> requestedScopes = null;
		// Se o escopo foi fornecido, divide a string de escopos em um conjunto.
		if (StringUtils.hasText(scope)) {
			requestedScopes = new HashSet<>(
					Arrays.asList(StringUtils.delimitedListToStringArray(scope, " ")));
		}

		// Coleta quaisquer parâmetros adicionais que não sejam grant_type ou scope.
		Map<String, Object> additionalParameters = new HashMap<>();
		parameters.forEach((key, value) -> {
			if (!key.equals(OAuth2ParameterNames.GRANT_TYPE) &&
					!key.equals(OAuth2ParameterNames.SCOPE)) {
				additionalParameters.put(key, value.get(0));
			}
		});

		// Obtém o principal do cliente (a autenticação do cliente OAuth2) do SecurityContext.
		// Isso representa o cliente que está fazendo a requisição de token.
		Authentication clientPrincipal = SecurityContextHolder.getContext().getAuthentication();

		// Retorna um novo CustomPasswordAuthenticationToken, que encapsula as credenciais
		// do usuário, os escopos solicitados e quaisquer parâmetros adicionais.
		// Este token será processado posteriormente por um AuthenticationProvider customizado.
		return new CustomPasswordAuthenticationToken(clientPrincipal, requestedScopes, additionalParameters);
	}

	// getParameters: Método auxiliar para converter o Map<String, String[]> retornado por
	// request.getParameterMap() em um MultiValueMap<String, String>.
	// Um MultiValueMap é útil porque um parâmetro HTTP pode ter múltiplos valores.
	private static MultiValueMap<String, String> getParameters(HttpServletRequest request) {

		Map<String, String[]> parameterMap = request.getParameterMap();
		MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>(parameterMap.size());
		// Itera sobre os parâmetros e adiciona cada valor ao MultiValueMap.
		parameterMap.forEach((key, values) -> {
			if (values.length > 0) {
				for (String value : values) {
					parameters.add(key, value);
				}
			}
		});
		return parameters;
	}
}