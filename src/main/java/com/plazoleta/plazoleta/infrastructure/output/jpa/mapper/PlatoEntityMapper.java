package com.plazoleta.plazoleta.infrastructure.output.jpa.mapper;

import com.plazoleta.plazoleta.domain.model.Plato;
import com.plazoleta.plazoleta.infrastructure.output.jpa.entity.PlatoEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PlatoEntityMapper {

    PlatoEntity toEntity(Plato plato);

    Plato toModel(PlatoEntity entity);
}