package com.plazoleta.plazoleta.infrastructure.output.http.dto;

/** Cuerpo del POST /trazabilidad (contrato acordado con Reinel). Los estados van como texto. */
public record LogTrazabilidadRequest(Long idPedido, Long idCliente, Long idRestaurante, Long idEmpleado,
                                     String estadoAnterior, String estadoNuevo) {
}