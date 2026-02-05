package com.devsuprior.dscatalog.resources;

import com.devsuprior.dscatalog.dto.ProductDTO;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

    private ProductDTO productDTO;

    // Page que simula uma resposta paginada da API
    private PageImpl<ProductDTO> page;

    @BeforeEach
    void setUp() throws Exception {

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
    }

    @Test
    public void findAllShouldReturnPage() throws Exception {
        // perform: Executa a requisição
        // get: Método HTTP GET na rota /products
        // accept: Define que o cliente aceita receber JSON como resposta
        ResultActions result = mockMvc.perform(get("/products")
                .accept(MediaType.APPLICATION_JSON));

        // Verifica se o status da resposta é 200 OK
        result.andExpect(status().isOk());
    }
}