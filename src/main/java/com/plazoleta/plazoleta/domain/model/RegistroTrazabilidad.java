package com.plazoleta.plazoleta.domain.model;

/**
 * Un cambio de estado de un pedido, tal como se reporta a Trazabilidad.
 * idEmpleado es nulo hasta que un empleado se asigna; estadoAnterior es nulo en el primer registro.
 */
public record RegistroTrazabilidad(Long idPedido, Long idCliente, Long idRestaurante, Long idEmpleado,
                                   EstadoPedido estadoAnterior, EstadoPedido estadoNuevo) {
}