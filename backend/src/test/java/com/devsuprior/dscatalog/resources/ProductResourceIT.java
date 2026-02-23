package com.devsuprior.dscatalog.resources;

import com.devsuprior.dscatalog.dto.ProductDTO;
import com.devsuprior.dscatalog.tests.Factory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// @SpringBootTest: Carrega o contexto completo da aplicação para um teste de integração.
// @AutoConfigureMockMvc: Configura o MockMvc para realizar requisições HTTP simuladas.
// @Transactional: Garante que cada teste rode em uma transação que será revertida (rollback) ao final,
// mantendo o estado do banco de dados limpo para o próximo teste.
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ProductResourceIT {

    @Autowired
    private MockMvc mockMvc;

    // ObjectMapper: Utilitário para converter objetos Java em JSON e vice-versa.
    @Autowired
    private ObjectMapper objectMapper;

    private Long existingId;

    private Long nonExistingId;

    private Long countTotalProducts;



    @BeforeEach
    void setUp() throws Exception {
        existingId = 1L;
        nonExistingId = 1000L;
        countTotalProducts = 25L;
    }

    @Test
    public void findAllShouldReturnSortedPageWhenSortByName() throws Exception{
        // ARRANGE: instancie os objetos necessários
        // (Neste caso, os dados de teste já estão no banco H2 e as variáveis no setUp)

        // ACT: execute as ações necessárias
        // Realiza uma requisição GET para /products com parâmetros de paginação e ordenação
        ResultActions result = mockMvc.perform(get("/products?page=0&size=12&sort=name,asc")
                .accept(MediaType.APPLICATION_JSON));

        // ASSERT: declare o que deveria acontecer (resultado esperado)
        // Verifica se o status da resposta é 200 OK
        result.andExpect(status().isOk());
        // Verifica se o número total de elementos na resposta corresponde ao esperado
        result.andExpect(jsonPath("$.totalElements").value(countTotalProducts));
        // Verifica se a lista de conteúdo (content) existe na resposta
        result.andExpect(jsonPath("$.content").exists());
        // Verifica a ordenação dos primeiros elementos pelo nome
        result.andExpect(jsonPath("$.content[0].name").value("Macbook Pro"));
        result.andExpect(jsonPath("$.content[1].name").value("PC Gamer"));
        result.andExpect(jsonPath("$.content[2].name").value("PC Gamer Alfa"));
    }

    @Test
    public void updateShouldReturnProductDTOWhenIdExists() throws Exception{
        // ARRANGE: instancie os objetos necessários
        // Cria um ProductDTO com dados de teste
        ProductDTO productDTO = Factory.createProductDTO();
        // Converte o DTO para JSON
        String jsonBody = objectMapper.writeValueAsString(productDTO);

        // Armazena os valores esperados para verificação posterior
        String expectedName = productDTO.getName();
        String expectedDescription = productDTO.getDescription();

        // ACT: execute as ações necessárias
        // Realiza uma requisição PUT para /products/{id} com o ID existente e o corpo JSON
        ResultActions result = mockMvc.perform(put("/products/{id}",existingId)
                .content(jsonBody)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));

        // ASSERT: declare o que deveria acontecer (resultado esperado)
        // Verifica se o status da resposta é 200 OK
        result.andExpect(status().isOk());
        // Verifica se o ID retornado é o mesmo que foi atualizado
        result.andExpect(jsonPath("$.id").value(existingId));
        // Verifica se o nome retornado corresponde ao esperado
        result.andExpect(jsonPath("$.name").value(expectedName));
        // Verifica se a descrição retornada corresponde ao esperado
        result.andExpect(jsonPath("$.description").value(expectedDescription));
    }

    @Test
    public void updateShouldReturnNotFoundWhenIdDoesNotExist() throws Exception {
        // ARRANGE: instancie os objetos necessários
        // Cria um ProductDTO com dados de teste
        ProductDTO productDTO = Factory.createProductDTO();
        // Converte o DTO para JSON
        String jsonBody = objectMapper.writeValueAsString(productDTO);

        // ACT: execute as ações necessárias
        // Realiza uma requisição PUT para /products/{id} com um ID inexistente
        ResultActions result = mockMvc.perform(put("/products/{id}", nonExistingId)
                .content(jsonBody)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));

        // ASSERT: declare o que deveria acontecer (resultado esperado)
        // Verifica se o status da resposta é 404 Not Found
        result.andExpect(status().isNotFound());
    }
}