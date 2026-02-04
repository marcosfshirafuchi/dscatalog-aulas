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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Carrega apenas a camada WEB (Controller)
// Não sobe todo o contexto Spring, nem banco, nem services reais
// Apenas o ProductResource e componentes necessários para MVC
@WebMvcTest(ProductResource.class)
public class ProductResourceTest {

    // MockMvc é usado para simular requisições HTTP
    // sem subir servidor real (Tomcat, Jetty, etc.)
    @Autowired
    private MockMvc mockMvc;

    // Cria um mock Spring do ProductService
    // Diferente do @Mock do Mockito: aqui o mock
    // é gerenciado pelo contexto Spring
    @MockitoBean
    private ProductService service;

    // DTO usado como resposta simulada
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

        // =========================
        // ACT (Ação)
        // =========================
        // Simula uma requisição HTTP GET para /products
        // usando o MockMvc
        mockMvc.perform(get("/products"))

                // =========================
                // ASSERT (Verificação)
                // =========================
                // Espera que a resposta HTTP seja 200 OK
                .andExpect(status().isOk());
    }
}