package com.plazoleta.plazoleta.infrastructure.input.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;


@Schema(description = "Datos que se pueden modificar de un plato: solo precio y descripción")
public record PlatoModificacionRequestDto(
        @Schema(description = "Nuevo precio: número entero mayor a 0", type = "integer", example = "30000",
                minimum = "1", requiredMode = REQUIRED) BigDecimal precio,
        @Schema(description = "Nueva descripción del plato", example = "Frijoles, arroz, carne, chicharrón y huevo",
                requiredMode = REQUIRED) String descripcion) {
}