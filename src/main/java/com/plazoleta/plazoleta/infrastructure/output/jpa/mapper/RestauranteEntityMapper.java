package com.plazoleta.plazoleta.infrastructure.output.jpa.mapper;

import com.plazoleta.plazoleta.domain.model.Restaurante;
import com.plazoleta.plazoleta.infrastructure.output.jpa.entity.RestauranteEntity;
import org.mapstruct.Mapper;


@Mapper
public interface RestauranteEntityMapper {

    RestauranteEntity toEntity(Restaurante restaurante);

    Restaurante toModel(RestauranteEntity entity);
}