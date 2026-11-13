package com.plazoleta.plazoleta.application.handler;

import com.plazoleta.plazoleta.domain.model.EstadoPedido;
import com.plazoleta.plazoleta.domain.model.PaginaRespuesta;
import com.plazoleta.plazoleta.domain.model.Pedido;

/** Puerto de entrada de la capa de aplicación para pedidos: lo único que conoce el controller. */
public interface PedidoHandler {

    Pedido realizarPedido(Pedido pedido);

    PaginaRespuesta<Pedido> listarPedidosPorEstado(Long idEmpleado, EstadoPedido estado, int pagina, int tamanio);
}