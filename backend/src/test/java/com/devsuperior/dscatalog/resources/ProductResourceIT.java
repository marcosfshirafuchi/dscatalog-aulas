package com.devsuperior.dscatalog.resources;

import com.devsuperior.dscatalog.dto.ProductDTO;
import com.devsuperior.dscatalog.tests.Factory;
import com.devsuperior.dscatalog.tests.TokenUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Classe de teste de integração para o recurso ProductResource.
 *
 * Teste de integração significa que o Spring sobe o contexto completo da aplicação,
 * permitindo testar o comportamento real das camadas Controller, Service,
 * Repository, banco de dados de teste e segurança.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ProductResourceIT {

    /**
     * MockMvc permite simular requisições HTTP para os endpoints da aplicação,
     * sem precisar subir um servidor real.
     */
    @Autowired
    private MockMvc mockMvc;

    /**
     * ObjectMapper converte objetos Java em JSON e JSON em objetos Java.
     * Aqui ele será usado para transformar ProductDTO em corpo JSON da requisição.
     */
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Classe utilitária criada para obter um token JWT válido nos testes.
     * Esse token será usado em endpoints protegidos por autenticação.
     */
    @Autowired
    private TokenUtil tokenUtil;

    /**
     * ID de um produto que já existe no banco de dados de teste.
     */
    private Long existingId;

    /**
     * ID de um produto que não existe no banco de dados.
     */
    private Long nonExistingId;

    /**
     * Quantidade total esperada de produtos cadastrados no banco de teste.
     */
    private Long countTotalProducts;

    /**
     * Dados de login usados para gerar o token de autenticação.
     */
    private String username, password, bearerToken;

    /**
     * Método executado antes de cada teste.
     *
     * Ele prepara os dados necessários para os testes,
     * evitando repetição de código dentro de cada método de teste.
     */
    @BeforeEach
    void setUp() throws Exception {
        existingId = 1L;
        nonExistingId = 1000L;
        countTotalProducts = 25L;

        username = "maria@gmail.com";
        password = "123456";

        // Obtém um token JWT válido para realizar requisições autenticadas.
        bearerToken = tokenUtil.obtainAccessToken(mockMvc, username, password);
    }

    @Test
    public void findAllShouldReturnSortedPageWhenSortByName() throws Exception {

        /*
         * ACT
         * Executa uma requisição GET para buscar produtos paginados.
         *
         * page=0      -> primeira página
         * size=12     -> até 12 produtos por página
         * sort=name,asc -> ordenação pelo nome em ordem crescente
         */
        ResultActions result = mockMvc.perform(get("/products?page=0&size=12&sort=name,asc")
                .accept(MediaType.APPLICATION_JSON));

        /*
         * ASSERT
         * Verifica se a resposta da API está correta.
         */
        result.andExpect(status().isOk());

        // Verifica se o total de produtos retornado na paginação é o esperado.
        result.andExpect(jsonPath("$.totalElements").value(countTotalProducts));

        // Verifica se existe o array "content", onde ficam os produtos da página.
        result.andExpect(jsonPath("$.content").exists());

        // Verifica se os primeiros produtos vieram ordenados corretamente pelo nome.
        result.andExpect(jsonPath("$.content[0].name").value("Macbook Pro"));
        result.andExpect(jsonPath("$.content[1].name").value("PC Gamer"));
        result.andExpect(jsonPath("$.content[2].name").value("PC Gamer Alfa"));
    }

    @Test
    public void updateShouldReturnProductDTOWhenIdExists() throws Exception {

        /*
         * ARRANGE
         * Prepara os dados necessários para atualizar um produto.
         */
        ProductDTO productDTO = Factory.createProductDTO();

        // Converte o objeto ProductDTO para JSON, pois o corpo da requisição PUT precisa estar em JSON.
        String jsonBody = objectMapper.writeValueAsString(productDTO);

        // Guarda os valores esperados para comparar com a resposta da API.
        String expectedName = productDTO.getName();
        String expectedDescription = productDTO.getDescription();

        /*
         * ACT
         * Executa uma requisição PUT para atualizar um produto existente.
         *
         * Também envia o token no cabeçalho Authorization,
         * pois esse endpoint exige autenticação.
         */
        ResultActions result = mockMvc.perform(put("/products/{id}", existingId)
                .header("Authorization", "Bearer " + bearerToken)
                .content(jsonBody)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));

        /*
         * ASSERT
         * Verifica se o produto foi atualizado corretamente.
         */
        result.andExpect(status().isOk());

        // Verifica se o ID retornado é o mesmo ID enviado na URL.
        result.andExpect(jsonPath("$.id").value(existingId));

        // Verifica se o nome retornado é o mesmo enviado no corpo da requisição.
        result.andExpect(jsonPath("$.name").value(expectedName));

        // Verifica se a descrição retornada é a mesma enviada no corpo da requisição.
        result.andExpect(jsonPath("$.description").value(expectedDescription));
    }

    @Test
    public void updateShouldReturnNotFoundWhenIdDoesNotExist() throws Exception {

        /*
         * ARRANGE
         * Cria um DTO válido, mas será usado com um ID inexistente.
         */
        ProductDTO productDTO = Factory.createProductDTO();

        // Converte o DTO para JSON para enviar no corpo da requisição.
        String jsonBody = objectMapper.writeValueAsString(productDTO);

        /*
         * ACT
         * Tenta atualizar um produto que não existe no banco de dados.
         */
        ResultActions result = mockMvc.perform(put("/products/{id}", nonExistingId)
                .header("Authorization", "Bearer " + bearerToken)
                .content(jsonBody)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));

        /*
         * ASSERT
         * Como o ID não existe, a API deve retornar 404 Not Found.
         */
        result.andExpect(status().isNotFound());
    }
}