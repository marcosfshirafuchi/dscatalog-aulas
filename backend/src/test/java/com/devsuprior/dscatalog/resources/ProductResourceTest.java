package com.devsuprior.dscatalog.resources;

import com.devsuprior.dscatalog.dto.ProductDTO;
import com.devsuprior.dscatalog.exceptions.ResourceNotFoundException;
import com.devsuprior.dscatalog.services.ProductService;
import com.devsuprior.dscatalog.tests.Factory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// @WebMvcTest: Carrega o contexto WEB para testar apenas a camada de Controller (ProductResource).
// Não carrega o contexto completo da aplicação (Repositories, Services reais, etc).
@WebMvcTest(ProductResource.class)
public class ProductResourceTest {

    // MockMvc: Objeto principal para realizar chamadas simuladas aos endpoints REST.
    @Autowired
    private MockMvc mockMvc;

    // @MockitoBean: Cria um Mock do ProductService e o injeta no contexto do Spring,
    // substituindo o bean real. Isso permite simular o comportamento do serviço.
    @MockitoBean
    private ProductService service;

    // ObjectMapper: Utilitário para converter objetos Java em JSON e vice-versa.
    @Autowired
    private ObjectMapper objectMapper;

    private Long existingId;

    private Long nonExistingId;

    private ProductDTO productDTO;

    // Page que simula uma resposta paginada da API
    private PageImpl<ProductDTO> page;

    @BeforeEach
    void setUp() throws Exception {

        existingId = 1L;
        nonExistingId = 2L;

        // Cria um ProductDTO fake usando a Factory
        // (classe utilitária comum em testes)
        productDTO = Factory.createProductDTO();

        // Cria uma página contendo apenas um item
        // Simula retorno do endpoint GET /products
        page = new PageImpl<>(List.of(productDTO));

        // Define comportamento do mock:
        // quando o controller chamar service.findAllPaged(...)
        // ele vai receber a página criada acima
        Mockito.when(service.findAllPaged(any())).thenReturn(page);

        // Configura o mock para retornar o DTO quando o ID existir
        Mockito.when(service.findById(existingId)).thenReturn(productDTO);
        
        // Configura o mock para lançar exceção quando o ID não existir
        Mockito.when(service.findById(nonExistingId)).thenThrow(ResourceNotFoundException.class);

        // Configura o mock para retornar o DTO atualizado quando o ID existir
        Mockito.when(service.update(eq(existingId), any())).thenReturn(productDTO);

        // Configura o mock para lançar exceção ao tentar atualizar um ID inexistente
        Mockito.when(service.update(eq(nonExistingId), any())).thenThrow(ResourceNotFoundException.class);

    }

    @Test
    public void updateShouldReturnProductDTOWhenIdExists() throws Exception{

        // ARRANGE: instancie os objetos necessários
        // Converte o objeto ProductDTO para uma String JSON
        String jsonBody = objectMapper.writeValueAsString(productDTO);

        // ACT: execute as ações necessárias
        // Realiza uma requisição PUT para /products/{id}
        // .content(jsonBody): Define o corpo da requisição com o JSON criado
        // .contentType(MediaType.APPLICATION_JSON): Define o cabeçalho Content-Type como application/json
        // .accept(MediaType.APPLICATION_JSON): Define que a resposta esperada é JSON
        ResultActions result = mockMvc.perform(put("/products/{id}",existingId)
                        .content(jsonBody)
                        .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));

        // ASSERT: declare o que deveria acontecer (resultado esperado)
        // Verifica se o status da resposta é 200 OK
        result.andExpect(status().isOk());
        // Verifica se os campos do JSON existem na resposta
        result.andExpect(jsonPath("$.id").exists());
        result.andExpect(jsonPath("$.name").exists());
        result.andExpect(jsonPath("$.description").exists());
    }

    @Test
    public void updateShouldReturnNotFoundWhenIdDoesNotExist() throws Exception{
        // ARRANGE: instancie os objetos necessários
        // Converte o objeto ProductDTO para uma String JSON
        String jsonBody = objectMapper.writeValueAsString(productDTO);
        
        // ACT: execute as ações necessárias
        // Realiza uma requisição PUT para /products/{id} com um ID inexistente
        ResultActions result = mockMvc.perform(put("/products/{id}",nonExistingId)
                .content(jsonBody)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));

        // ASSERT: declare o que deveria acontecer (resultado esperado)
        // Verifica se o status da resposta é 404 Not Found
        result.andExpect(status().isNotFound());
    }

    @Test
    public void findAllShouldReturnPage() throws Exception {
        // ARRANGE: instancie os objetos necessários
        // (Neste caso, o setUp já configurou o comportamento do service.findAllPaged)

        // ACT: execute as ações necessárias
        // perform: Executa a requisição
        // get: Método HTTP GET na rota /products
        // accept: Define que o cliente aceita receber JSON como resposta
        ResultActions result = mockMvc.perform(get("/products")
                .accept(MediaType.APPLICATION_JSON));

        // ASSERT: declare o que deveria acontecer (resultado esperado)
        // Verifica se o status da resposta é 200 OK
        result.andExpect(status().isOk());
    }

    @Test
    public void findByIdShouldReturnProductWhenIdExists() throws Exception{
        // ARRANGE: instancie os objetos necessários
        // (O ID existente já foi configurado no setUp)

        // ACT: execute as ações necessárias
        // Realiza a requisição GET para /products/{id} passando o ID existente
        ResultActions result = mockMvc.perform(get("/products/{id}", existingId)
                .accept(MediaType.APPLICATION_JSON));

        // ASSERT: declare o que deveria acontecer (resultado esperado)
        // Verifica se o status é 200 OK
        result.andExpect(status().isOk());
        
        // Verifica se os campos do JSON existem na resposta
        result.andExpect(jsonPath("$.id").exists());
        result.andExpect(jsonPath("$.name").exists());
        result.andExpect(jsonPath("$.description").exists());
    }

    @Test
    public void findByIdShouldReturnNotFoundWhenIdDoesNotExists() throws Exception {
        // ARRANGE: instancie os objetos necessários
        // (O ID não existente já foi configurado no setUp para lançar exceção)

        // ACT: execute as ações necessárias
        // Realiza a requisição GET para /products/{id} passando o ID inexistente
        ResultActions result = mockMvc.perform(get("/products/{id}", nonExistingId)
                .accept(MediaType.APPLICATION_JSON));

        // ASSERT: declare o que deveria acontecer (resultado esperado)
        // Verifica se o status é 404 Not Found
        result.andExpect(status().isNotFound());
    }
}