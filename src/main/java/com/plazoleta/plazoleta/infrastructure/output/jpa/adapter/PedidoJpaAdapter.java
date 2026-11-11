package com.plazoleta.plazoleta.infrastructure.output.jpa.adapter;

import com.plazoleta.plazoleta.domain.model.EstadoPedido;
import com.plazoleta.plazoleta.domain.model.Pedido;
import com.plazoleta.plazoleta.domain.spi.PedidoPersistencePort;
import com.plazoleta.plazoleta.infrastructure.output.jpa.entity.PedidoEntity;
import com.plazoleta.plazoleta.infrastructure.output.jpa.mapper.PedidoEntityMapper;
import com.plazoleta.plazoleta.infrastructure.output.jpa.repository.PedidoRepository;

import java.util.List;

public class PedidoJpaAdapter implements PedidoPersistencePort {

    private final PedidoRepository pedidoRepository;
    private final PedidoEntityMapper pedidoEntityMapper;

    public PedidoJpaAdapter(PedidoRepository pedidoRepository, PedidoEntityMapper pedidoEntityMapper) {
        this.pedidoRepository = pedidoRepository;
        this.pedidoEntityMapper = pedidoEntityMapper;
    }

    @Override
    public Pedido guardarPedido(Pedido pedido) {
        PedidoEntity guardado = pedidoRepository.save(pedidoEntityMapper.toEntity(pedido));
        return pedidoEntityMapper.toModel(guardado);
    }

    @Override
    public boolean existePedidoDeClienteEnEstados(Long idCliente, List<EstadoPedido> estados) {
        return pedidoRepository.existsByIdClienteAndEstadoIn(idCliente, estados);
    }
}