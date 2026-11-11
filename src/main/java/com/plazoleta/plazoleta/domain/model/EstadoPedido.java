package com.plazoleta.plazoleta.domain.model;

import java.util.List;

public enum EstadoPedido {
    PENDIENTE,
    EN_PREPARACION,
    LISTO,
    ENTREGADO,
    CANCELADO;

    /** Estados en los que el pedido todavía no termina: impiden que el cliente haga otro pedido. */
    public static final List<EstadoPedido> EN_PROCESO = List.of(PENDIENTE, EN_PREPARACION, LISTO);
}