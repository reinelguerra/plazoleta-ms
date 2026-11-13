package com.plazoleta.plazoleta.domain.api;

import com.plazoleta.plazoleta.domain.model.EstadoPedido;
import com.plazoleta.plazoleta.domain.model.PaginaRespuesta;
import com.plazoleta.plazoleta.domain.model.Pedido;

public interface PedidoServicePort {

    /** Crea el pedido en estado PENDIENTE. El pedido trae idCliente, idRestaurante y los platos con cantidad. */
    Pedido realizarPedido(Pedido pedido);

    /** Pedidos del restaurante al que pertenece el empleado, filtrados por estado. */
    PaginaRespuesta<Pedido> listarPedidosPorEstado(Long idEmpleado, EstadoPedido estado, int pagina, int tamanio);

    /** El empleado toma un pedido PENDIENTE de su restaurante y lo pasa a EN_PREPARACION. */
    Pedido iniciarPreparacion(Long idPedido, Long idEmpleado);
}