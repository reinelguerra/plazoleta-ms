package com.plazoleta.plazoleta.infrastructure.exception;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Formato de error de la API")
public record ErrorResponse(
        @Schema(description = "Explicación del error", example = "No existe un plato con id 99") String mensaje,
        @Schema(description = "Código HTTP del error", example = "404") int status) {
}