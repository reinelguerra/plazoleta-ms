package com.plazoleta.plazoleta.infrastructure.input.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Plato de un pedido")
public record PedidoPlatoResponseDto(
        @Schema(description = "Id del plato", example = "1") Long idPlato,
        @Schema(description = "Cantidad pedida", example = "2") Integer cantidad) {
}