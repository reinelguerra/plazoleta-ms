package com.plazoleta.plazoleta.infrastructure.input.rest.dto;

import com.plazoleta.plazoleta.domain.util.MensajeError;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Schema(description = "Nuevo estado de un plato")
public record PlatoEstadoRequestDto(
        @NotNull(message = MensajeError.ESTADO_PLATO_OBLIGATORIO)
        @Schema(description = "true habilita el plato y false lo deshabilita", example = "false",
                requiredMode = REQUIRED) Boolean activo) {
}