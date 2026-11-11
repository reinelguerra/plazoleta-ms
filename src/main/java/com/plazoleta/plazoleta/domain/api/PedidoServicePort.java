package com.plazoleta.plazoleta.domain.api;

import com.plazoleta.plazoleta.domain.model.Pedido;

public interface PedidoServicePort {

    /** Crea el pedido en estado PENDIENTE. El pedido trae idCliente, idRestaurante y los platos con cantidad. */
    Pedido realizarPedido(Pedido pedido);
}