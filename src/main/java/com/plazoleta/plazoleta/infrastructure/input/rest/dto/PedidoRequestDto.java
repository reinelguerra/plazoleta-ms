package com.plazoleta.plazoleta.infrastructure.input.rest.dto;

import com.plazoleta.plazoleta.domain.util.MensajeError;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Schema(description = "Datos para realizar un pedido. El cliente sale del token")
public record PedidoRequestDto(
        @NotNull(message = MensajeError.RESTAURANTE_OBLIGATORIO)
        @Schema(description = "Id del restaurante: todos los platos deben ser de este restaurante",
                example = "1", requiredMode = REQUIRED) Long idRestaurante,
        @NotEmpty(message = MensajeError.PEDIDO_SIN_PLATOS)
        @Valid
        @Schema(description = "Platos pedidos con su cantidad", requiredMode = REQUIRED)
        List<PedidoPlatoRequestDto> platos) {
}