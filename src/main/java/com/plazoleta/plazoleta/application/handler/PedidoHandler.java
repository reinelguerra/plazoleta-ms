package com.plazoleta.plazoleta.application.handler;

import com.plazoleta.plazoleta.domain.model.Pedido;

/** Puerto de entrada de la capa de aplicación para pedidos: lo único que conoce el controller. */
public interface PedidoHandler {

    Pedido realizarPedido(Pedido pedido);
}