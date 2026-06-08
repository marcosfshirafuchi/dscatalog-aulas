package com.devsuperior.dscatalog.projections;

public interface ProjectProjection extends IdProjection<Long> {
    //Dados que vão retornar do banco de dados
    String getName();
}
