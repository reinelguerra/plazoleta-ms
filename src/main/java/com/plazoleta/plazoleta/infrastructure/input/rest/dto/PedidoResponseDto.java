package com.plazoleta.plazoleta.infrastructure.input.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Pedido. Lo devuelven la creación, el listado por estado, la toma de un pedido por un "
        + "empleado y la cancelación. Nunca incluye el pin de entrega")
public record PedidoResponseDto(
        @Schema(description = "Identificador del pedido", example = "1") Long id,
        @Schema(description = "Id del cliente dueño del pedido", example = "5") Long idCliente,
        @Schema(description = "Id del restaurante", example = "1") Long idRestaurante,
        @Schema(description = "Estado del pedido. Nace PENDIENTE; un empleado lo pasa a EN_PREPARACION y el "
                + "cliente puede cancelarlo solo mientras esté PENDIENTE",
                example = "PENDIENTE",
                allowableValues = {"PENDIENTE", "EN_PREPARACION", "LISTO", "ENTREGADO", "CANCELADO"}) String estado,
        @Schema(description = "Id del empleado que tomó el pedido. Vacío mientras esté PENDIENTE o si se "
                + "canceló antes de tomarse", example = "12", nullable = true) Long idEmpleado,
        @Schema(description = "Fecha y hora de creación del pedido", example = "2026-11-13T10:30:00") LocalDateTime fecha,
        @Schema(description = "Platos del pedido con su cantidad") List<PedidoPlatoResponseDto> platos) {
}