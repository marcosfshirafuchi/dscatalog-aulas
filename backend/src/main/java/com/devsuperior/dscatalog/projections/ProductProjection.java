package com.devsuperior.dscatalog.projections;

public interface ProductProjection extends IdProjection<Long> {
    //Dados que vão retornar do banco de dados
    String getName();
}
