package com.plazoleta.plazoleta.infrastructure.input.rest.mapper;

import com.plazoleta.plazoleta.domain.model.Restaurante;
import com.plazoleta.plazoleta.infrastructure.input.rest.dto.RestauranteRequest;
import com.plazoleta.plazoleta.infrastructure.input.rest.dto.RestauranteResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/** Se registra a mano como bean en RestauranteBeanConfiguration, igual que el mapper de JPA. */
@Mapper
public interface RestauranteRequestMapper {

    @Mapping(target = "id", ignore = true)
    Restaurante toModel(RestauranteRequest request);

    RestauranteResponse toResponse(Restaurante restaurante);
}