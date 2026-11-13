package com.plazoleta.plazoleta.application.handler;

import com.plazoleta.plazoleta.domain.model.EstadoPedido;
import com.plazoleta.plazoleta.domain.model.PaginaRespuesta;
import com.plazoleta.plazoleta.domain.model.Pedido;

/** Puerto de entrada de la capa de aplicación para pedidos: lo único que conoce el controller. */
public interface PedidoHandler {

    Pedido realizarPedido(Pedido pedido);

    PaginaRespuesta<Pedido> listarPedidosPorEstado(Long idEmpleado, EstadoPedido estado, int pagina, int tamanio);

    Pedido iniciarPreparacion(Long idPedido, Long idEmpleado);

    /** El cliente cancela su pedido mientras este PENDIENTE. */
    Pedido cancelarPedido(Long idPedido, Long idCliente);
}