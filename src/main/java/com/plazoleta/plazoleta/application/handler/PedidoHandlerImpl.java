package com.plazoleta.plazoleta.application.handler;

import com.plazoleta.plazoleta.domain.api.PedidoServicePort;
import com.plazoleta.plazoleta.domain.model.EstadoPedido;
import com.plazoleta.plazoleta.domain.model.PaginaRespuesta;
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

    @Override
    public PaginaRespuesta<Pedido> listarPedidosPorEstado(Long idEmpleado, EstadoPedido estado,
                                                          int pagina, int tamanio) {
        return pedidoServicePort.listarPedidosPorEstado(idEmpleado, estado, pagina, tamanio);
    }

    @Override
    public Pedido iniciarPreparacion(Long idPedido, Long idEmpleado) {
        return pedidoServicePort.iniciarPreparacion(idPedido, idEmpleado);
    }

    @Override
    public Pedido cancelarPedido(Long idPedido, Long idCliente) {
        return pedidoServicePort.cancelarPedido(idPedido, idCliente);
    }
}