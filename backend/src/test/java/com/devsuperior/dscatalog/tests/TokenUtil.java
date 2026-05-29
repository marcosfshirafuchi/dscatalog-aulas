package com.devsuperior.dscatalog.tests; // Declaração do pacote onde a classe TokenUtil está localizada.

// Importações estáticas para facilitar a escrita de testes com MockMvc.
// Permitem chamar métodos como 'post', 'status', 'content' diretamente.
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic; // Importa método para autenticação HTTP Basic em requisições MockMvc.
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content; // Importa matcher para verificar o conteúdo da resposta HTTP.
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post; // Importa método para construir requisições HTTP POST.
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status; // Importa matcher para verificar o status HTTP da resposta.

import org.springframework.beans.factory.annotation.Value; // Importa a anotação @Value para injetar valores de propriedades.
import org.springframework.boot.json.JacksonJsonParser; // Importa um parser JSON para manipular respostas JSON.
import org.springframework.stereotype.Component; // Importa a anotação @Component para que o Spring gerencie esta classe.
import org.springframework.test.web.servlet.MockMvc; // Importa MockMvc para simular requisições HTTP em testes.
import org.springframework.test.web.servlet.ResultActions; // Importa ResultActions para encadear verificações na resposta de MockMvc.
import org.springframework.util.LinkedMultiValueMap; // Importa implementação de MultiValueMap que mantém a ordem de inserção.
import org.springframework.util.MultiValueMap; // Importa interface para um mapa que pode ter múltiplos valores para uma única chave.

// @Component: Indica que esta classe é um componente genérico do Spring.
// O Spring irá detectá-la e gerenciá-la, permitindo a injeção de dependências e o uso de anotações como @Value.
@Component
public class TokenUtil {

    // @Value("${security.client-id}"): Injeta o valor da propriedade 'security.client-id'
    // (geralmente definida em application.properties ou application.yml) no campo clientId.
    @Value("${security.client-id}")
    private String clientId;

    // @Value("${security.client-secret}"): Injeta o valor da propriedade 'security.client-secret'
    // no campo clientSecret.
    @Value("${security.client-secret}")
    private String clientSecret;

    /**
     * Obtém um token de acesso OAuth2 simulando uma requisição de login.
     * Este método é útil em testes de integração para autenticar usuários e obter um token.
     *
     * @param mockMvc Uma instância de MockMvc para simular a requisição HTTP.
     * @param username O nome de usuário (email) para autenticação.
     * @param password A senha do usuário para autenticação.
     * @return O token de acesso (access_token) como uma String.
     * @throws Exception Se ocorrer um erro durante a simulação da requisição ou parsing do JSON.
     */
    public String obtainAccessToken(MockMvc mockMvc, String username, String password) throws Exception {

        // Cria um MultiValueMap para armazenar os parâmetros da requisição POST.
        // MultiValueMap é usado porque alguns parâmetros podem ter múltiplos valores (embora não neste caso específico).
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        // Adiciona os parâmetros necessários para a requisição de token OAuth2 com grant_type "password".
        params.add("grant_type", "password"); // Tipo de concessão de token (password credentials).
        params.add("client_id", clientId);     // ID do cliente OAuth2.
        params.add("username", username);     // Nome de usuário para autenticação.
        params.add("password", password);     // Senha do usuário para autenticação.

        // Simula a execução de uma requisição HTTP POST para o endpoint de token OAuth2.
        ResultActions result = mockMvc
                .perform(post("/oauth2/token") // Constrói uma requisição POST para "/oauth2/token".
                        .params(params)       // Adiciona os parâmetros definidos acima à requisição.
                        // Adiciona autenticação HTTP Basic usando o clientId e clientSecret.
                        // Isso é necessário para autenticar o cliente OAuth2.
                        .with(httpBasic(clientId, clientSecret))
                        // Define o cabeçalho "Accept" para indicar que o cliente espera uma resposta JSON com charset UTF-8.
                        .accept("application/json;charset=UTF-8"))
                // Verifica se o status da resposta HTTP é 200 OK.
                .andExpect(status().isOk())
                // Verifica se o cabeçalho "Content-Type" da resposta é "application/json;charset=UTF-8".
                .andExpect(content().contentType("application/json;charset=UTF-8"));

        // Extrai o corpo da resposta HTTP como uma String.
        String resultString = result.andReturn().getResponse().getContentAsString();

        // Cria um JacksonJsonParser para analisar a String JSON da resposta.
        JacksonJsonParser jsonParser = new JacksonJsonParser();
        // Analisa a String JSON, obtém o valor associado à chave "access_token" e o retorna como String.
        return jsonParser.parseMap(resultString).get("access_token").toString();
    }
}