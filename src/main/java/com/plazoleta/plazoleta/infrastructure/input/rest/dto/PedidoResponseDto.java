package com.plazoleta.plazoleta.infrastructure.input.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Pedido creado")
public record PedidoResponseDto(
        @Schema(description = "Identificador del pedido", example = "1") Long id,
        @Schema(description = "Id del cliente (el del token)", example = "5") Long idCliente,
        @Schema(description = "Id del restaurante", example = "1") Long idRestaurante,
        @Schema(description = "Estado del pedido. Al crearse es PENDIENTE", example = "PENDIENTE") String estado,
        @Schema(description = "Empleado asignado (vacío hasta que un empleado se asigna)") Long idEmpleado,
        @Schema(description = "Fecha y hora de creación") LocalDateTime fecha,
        @Schema(description = "Platos del pedido") List<PedidoPlatoResponseDto> platos) {
}