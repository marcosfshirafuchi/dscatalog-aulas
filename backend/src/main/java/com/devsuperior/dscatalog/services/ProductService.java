package com.devsuperior.dscatalog.services;

import com.devsuperior.dscatalog.dto.CategoryDTO;
import com.devsuperior.dscatalog.dto.ProductDTO;
import com.devsuperior.dscatalog.entities.Category;
import com.devsuperior.dscatalog.entities.Product;
import com.devsuperior.dscatalog.projections.ProductProjection;
import com.devsuperior.dscatalog.services.exceptions.DatabaseException;
import com.devsuperior.dscatalog.services.exceptions.ResourceNotFoundException;
import com.devsuperior.dscatalog.repositories.CategoryRepository;
import com.devsuperior.dscatalog.repositories.ProductRepository;
import com.devsuperior.dscatalog.util.Utils;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

// @Service indica que esta classe pertence à camada de serviço da aplicação.
// A camada de serviço concentra as regras de negócio.
@Service
public class ProductService {

    // Injeta automaticamente o ProductRepository.
    // O repository é responsável por acessar os dados de Product no banco.
    @Autowired
    private ProductRepository repository;

    // Injeta o CategoryRepository.
    // Ele será usado para buscar as categorias relacionadas ao produto.
    @Autowired
    private CategoryRepository categoryRepository;

    // readOnly = true indica que este método apenas consulta dados.
    // Isso melhora a performance, pois não há intenção de alterar o banco.
    @Transactional(readOnly = true)
    public Page<ProductDTO> findAllPaged(Pageable pageable) {

        // Busca todos os produtos no banco de forma paginada.
        Page<Product> list = repository.findAll(pageable);

        // Converte cada Product em ProductDTO usando o método map.
        // Isso evita expor diretamente a entidade para a camada de controller.
        Page<ProductDTO> listDto = list.map(x -> new ProductDTO(x));

        // Retorna a página de DTOs.
        return listDto;
    }

    // Consulta um produto pelo id.
    @Transactional(readOnly = true)
    public ProductDTO findById(Long id) {

        // Busca o produto no banco.
        // Como o resultado pode existir ou não, o retorno é Optional<Product>.
        Optional<Product> obj = repository.findById(id);

        // Se o produto existir, retorna a entidade.
        // Se não existir, lança uma exceção personalizada ResourceNotFoundException.
        Product entity = obj.orElseThrow(() -> new ResourceNotFoundException("Entity not found"));

        // Converte a entidade Product para ProductDTO, incluindo suas categorias.
        return new ProductDTO(entity, entity.getCategories());
    }

    // Método responsável por inserir um novo produto no banco.
    @Transactional
    public ProductDTO insert(ProductDTO dto) {

        // Cria uma nova entidade Product vazia.
        Product entity = new Product();

        // Copia os dados recebidos no DTO para a entidade.
        copyDtoToEntity(dto, entity);

        // Salva a entidade no banco de dados.
        entity = repository.save(entity);

        // Retorna o produto salvo em formato DTO.
        return new ProductDTO(entity, entity.getCategories());
    }

    // Método responsável por atualizar um produto existente.
    @Transactional
    public ProductDTO update(Long id, ProductDTO dto) {
        try {

            // getReferenceById busca uma referência do produto pelo id.
            // Ele não acessa imediatamente o banco, apenas cria uma referência gerenciada pelo JPA.
            Product entity = repository.getReferenceById(id);

            // Copia os novos dados do DTO para a entidade existente.
            copyDtoToEntity(dto, entity);

            // Salva as alterações no banco.
            entity = repository.save(entity);

            // Retorna o produto atualizado em formato DTO.
            return new ProductDTO(entity, entity.getCategories());

        } catch (EntityNotFoundException e) {

            // Caso o id não exista no banco, lança uma exceção personalizada.
            throw new ResourceNotFoundException("Id not found " + id);
        }
    }

    // Propagation.SUPPORTS indica que o método participa de uma transação se já existir.
    // Caso contrário, ele executa sem criar uma nova transação.
    @Transactional(propagation = Propagation.SUPPORTS)
    public void delete(Long id) {

        // Antes de deletar, verifica se o produto existe.
        // Isso permite lançar uma exceção mais clara caso o id não seja encontrado.
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Recurso não encontrado");
        }

        try {

            // Deleta o produto pelo id.
            repository.deleteById(id);

        } catch (DataIntegrityViolationException e) {

            // Essa exceção ocorre quando o produto não pode ser deletado
            // porque está relacionado com outros registros no banco.
            throw new DatabaseException("Falha de integridade referencial");
        }
    }

    // Método auxiliar para copiar os dados do DTO para a entidade.
    // Ele é usado tanto no insert quanto no update para evitar repetição de código.
    private void copyDtoToEntity(ProductDTO dto, Product entity) {

        // Copia os atributos simples do produto.
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setDate(dto.getDate());
        entity.setImgUrl(dto.getImgUrl());
        entity.setPrice(dto.getPrice());

        // Limpa as categorias atuais da entidade.
        // Isso é importante no update para remover categorias antigas antes de adicionar as novas.
        entity.getCategories().clear();

        // Percorre todas as categorias recebidas no DTO.
        for (CategoryDTO catDto : dto.getCategories()) {

            // Busca uma referência da categoria pelo id.
            // Não é necessário carregar todos os dados da categoria nesse momento.
            Category category = categoryRepository.getReferenceById(catDto.getId());

            // Adiciona a categoria ao produto.
            entity.getCategories().add(category);
        }
    }

    // Método de busca paginada com filtros por nome e categorias.
    @Transactional(readOnly = true)
    public Page<ProductDTO> findAllPaged(String name, String categoryId, Pageable pageable) {

        // Cria uma lista vazia de ids de categorias.
        List<Long> categoryIds = Arrays.asList();

        // Se categoryId for diferente de "0", significa que existe filtro por categoria.
        if (!"0".equals(categoryId)) {

            // Divide a String recebida por vírgula.
            // Exemplo: "1,2,3" vira ["1", "2", "3"].
            String[] vet = categoryId.split(",");

            // Converte o array em lista de String.
            List<String> list = Arrays.asList(vet);

            // Converte cada String para Long.
            // Exemplo: ["1", "2", "3"] vira [1L, 2L, 3L].
            categoryIds = list.stream()
                    .map(x -> Long.parseLong(x))
                    .toList();
        }

        // Faz a busca paginada utilizando uma projeção.
        //
        // Nesta etapa NÃO carregamos o objeto Product completo.
        // Apenas os campos necessários da projeção são retornados.
        //
        // Isso é importante porque não é recomendado utilizar JOIN FETCH
        // diretamente em consultas paginadas com relacionamentos @ManyToMany,
        // pois isso pode gerar duplicidades e problemas de paginação.
        //
        // O resultado desta consulta contém apenas os IDs dos produtos
        // que pertencem à página solicitada.
        Page<ProductProjection> page = repository.searchProducts(categoryIds, name, pageable);

        // Extrai os IDs dos produtos retornados pela consulta paginada.
        //
        // Exemplo:
        //
        // Página retornada:
        // [3, 7, 1]
        //
        // Resultado:
        //
        // [3L, 7L, 1L]
        List<Long> productIds = page.map(x -> x.getId()).toList();

        // Agora que sabemos exatamente quais produtos pertencem à página,
        // realizamos uma segunda consulta.
        //
        // Desta vez utilizamos JOIN FETCH para carregar os produtos
        // juntamente com suas categorias.
        //
        // Isso evita o problema conhecido como:
        //
        // N + 1 Queries
        //
        // Sem o JOIN FETCH:
        //
        // 1 consulta para buscar produtos
        // +
        // N consultas para buscar categorias
        //
        // Com JOIN FETCH:
        //
        // Apenas uma consulta carrega tudo.
        List<Product> entities = repository.searchProductsWithCategories(productIds);

        // IMPORTANTE:
        //
        // O banco de dados não garante que os registros retornados
        // pela consulta com JOIN FETCH estarão na mesma ordem da
        // consulta paginada original.
        //
        // Exemplo:
        //
        // Consulta paginada:
        // [3, 7, 1]
        //
        // Consulta JOIN FETCH:
        // [1, 3, 7]
        //
        // Para resolver esse problema utilizamos o método Utils.replace(),
        // que reorganiza os elementos utilizando os IDs como referência,
        // preservando a ordem correta da paginação.
        entities = (List<Product>) Utils.replace(page.getContent(), entities);

        // Converte cada entidade Product para ProductDTO.
        //
        // Neste momento as categorias já estão carregadas,
        // evitando consultas adicionais ao banco.
        List<ProductDTO> dtos = entities.stream()
                .map(p -> new ProductDTO(p, p.getCategories()))
                .toList();

        // Cria uma nova página de DTOs mantendo:
        // - os dados convertidos
        // - a paginação original
        // - o total de elementos encontrados
        Page<ProductDTO> pageDTO = new PageImpl<>(dtos, page.getPageable(), page.getTotalElements());

        // Cria uma nova página de DTOs.
        //
        // Mantemos:
        //
        // - Os DTOs convertidos
        // - As informações de paginação (Pageable)
        //
        // Dessa forma o Controller recebe uma página completa
        // exatamente como o Spring Data espera.
        return pageDTO;
    }
}