package com.plazoleta.plazoleta.application.handler;

import com.plazoleta.plazoleta.domain.api.PedidoServicePort;
import com.plazoleta.plazoleta.domain.model.Pedido;

public class PedidoHandlerImpl implements PedidoHandler {

    private final PedidoServicePort pedidoServicePort;

    public PedidoHandlerImpl(PedidoServicePort pedidoServicePort) {
        this.pedidoServicePort = pedidoServicePort;
    }

    @Override
    public Pedido realizarPedido(Pedido pedido) {
        return pedidoServicePort.realizarPedido(pedido);
    }
}