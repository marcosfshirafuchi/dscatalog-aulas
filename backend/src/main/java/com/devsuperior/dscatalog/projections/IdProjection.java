package com.devsuperior.dscatalog.projections; // Declaração do pacote onde a interface IdProjection está localizada.

// public interface IdProjection<E>:
// Define uma interface Java chamada IdProjection.
// <E>: Indica que esta é uma interface genérica. O 'E' é um tipo de parâmetro que será substituído
// por um tipo real (como Long, Integer, String, etc.) quando a interface for implementada ou usada.
// O uso de genéricos permite que esta interface seja reutilizável para diferentes tipos de IDs.
//
// No contexto do Spring Data JPA, interfaces de projeção são usadas para buscar apenas um subconjunto
// das colunas de uma entidade, em vez de carregar a entidade completa. Isso otimiza o desempenho
// ao reduzir a quantidade de dados transferidos do banco de dados e o consumo de memória.
// Esta interface específica é projetada para extrair apenas o ID de uma entidade.
public interface IdProjection<E> {

    // E getId():
    // Declara um método abstrato chamado 'getId'.
    // O tipo de retorno 'E' é o tipo genérico definido na interface, o que significa que
    // este método retornará o ID no tipo especificado (ex: Long para um ID numérico).
    // Interfaces de projeção no Spring Data JPA automaticamente mapeiam o nome do método
    // (seguindo a convenção get<NomeDaPropriedade>) para a coluna correspondente no banco de dados.
    // Assim, 'getId()' buscará a coluna 'id' da tabela.
    E getId();
}