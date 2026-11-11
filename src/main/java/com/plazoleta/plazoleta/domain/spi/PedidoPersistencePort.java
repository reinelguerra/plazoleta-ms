package com.plazoleta.plazoleta.domain.spi;

import com.plazoleta.plazoleta.domain.model.EstadoPedido;
import com.plazoleta.plazoleta.domain.model.Pedido;

import java.util.List;

public interface PedidoPersistencePort {

    Pedido guardarPedido(Pedido pedido);

    boolean existePedidoDeClienteEnEstados(Long idCliente, List<EstadoPedido> estados);
}