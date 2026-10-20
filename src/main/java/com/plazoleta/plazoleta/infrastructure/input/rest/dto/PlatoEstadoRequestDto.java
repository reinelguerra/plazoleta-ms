package com.plazoleta.plazoleta.infrastructure.input.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Schema(description = "Nuevo estado del plato")
public record PlatoEstadoRequestDto(
        @Schema(description = "true habilita el plato y false lo deshabilita", example = "false",
                requiredMode = REQUIRED) Boolean activo) {
}