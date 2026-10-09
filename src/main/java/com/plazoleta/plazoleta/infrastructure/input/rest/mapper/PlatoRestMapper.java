package com.plazoleta.plazoleta.infrastructure.input.rest.mapper;

import com.plazoleta.plazoleta.domain.exception.DomainException;
import com.plazoleta.plazoleta.domain.model.Plato;
import com.plazoleta.plazoleta.infrastructure.input.rest.dto.PlatoRequestDto;
import com.plazoleta.plazoleta.infrastructure.input.rest.dto.PlatoResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.math.BigDecimal;

@Mapper(componentModel = "spring")
public interface PlatoRestMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "activo", ignore = true)
    @Mapping(target = "precio", qualifiedByName = "precioEntero")
    Plato toModel(PlatoRequestDto request);

    PlatoResponseDto toResponse(Plato plato);

    @Named("precioEntero")
    default Integer precioEntero(BigDecimal precio) {
        if (precio == null) {
            return null;
        }
        try {
            return precio.intValueExact();
        } catch (ArithmeticException e) {
            throw new DomainException("El precio debe ser un número entero mayor a 0");
        }
    }
}