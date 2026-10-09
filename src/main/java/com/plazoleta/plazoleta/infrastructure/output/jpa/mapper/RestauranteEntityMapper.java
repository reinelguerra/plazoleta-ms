package com.plazoleta.plazoleta.infrastructure.output.jpa.mapper;

import com.plazoleta.plazoleta.domain.model.Restaurante;
import com.plazoleta.plazoleta.infrastructure.output.jpa.entity.RestauranteEntity;
import org.mapstruct.Mapper;

/**
 * MapStruct genera la implementación al compilar (en target/generated-sources).
 * Como los campos se llaman igual en ambas clases, no hace falta configurar nada más.
 */
@Mapper
public interface RestauranteEntityMapper {

    RestauranteEntity toEntity(Restaurante restaurante);

    Restaurante toModel(RestauranteEntity entity);
}