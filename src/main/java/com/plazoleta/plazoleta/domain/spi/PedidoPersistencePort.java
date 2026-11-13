package com.plazoleta.plazoleta.domain.spi;

import com.plazoleta.plazoleta.domain.model.EstadoPedido;
import com.plazoleta.plazoleta.domain.model.PaginaRespuesta;
import com.plazoleta.plazoleta.domain.model.Pedido;

import java.util.List;
import java.util.Optional;

public interface PedidoPersistencePort {

    Pedido guardarPedido(Pedido pedido);

    Optional<Pedido> obtenerPedidoPorId(Long idPedido);

    boolean existePedidoDeClienteEnEstados(Long idCliente, List<EstadoPedido> estados);

    /** Del más antiguo al más reciente. */
    PaginaRespuesta<Pedido> listarPedidosPorRestauranteYEstado(Long idRestaurante, EstadoPedido estado,
                                                               int pagina, int tamanio);
}