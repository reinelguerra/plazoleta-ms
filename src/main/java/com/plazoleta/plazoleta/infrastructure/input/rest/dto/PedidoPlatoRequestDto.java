package com.plazoleta.plazoleta.infrastructure.input.rest.dto;

import com.plazoleta.plazoleta.domain.util.MensajeError;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Schema(description = "Un plato del pedido con su cantidad")
public record PedidoPlatoRequestDto(
        @NotNull(message = MensajeError.PLATO_PEDIDO_INVALIDO)
        @Schema(description = "Id del plato", example = "1", requiredMode = REQUIRED) Long idPlato,
        @NotNull(message = MensajeError.CANTIDAD_INVALIDA)
        @Positive(message = MensajeError.CANTIDAD_INVALIDA)
        @Schema(description = "Cantidad: entero mayor a 0", example = "2", minimum = "1",
                requiredMode = REQUIRED) Integer cantidad) {
}