package com.plazoleta.plazoleta.infrastructure.output.jpa.adapter;

import com.plazoleta.plazoleta.domain.model.EstadoPedido;
import com.plazoleta.plazoleta.domain.model.PaginaRespuesta;
import com.plazoleta.plazoleta.domain.model.Pedido;
import com.plazoleta.plazoleta.domain.spi.PedidoPersistencePort;
import com.plazoleta.plazoleta.infrastructure.output.jpa.entity.PedidoEntity;
import com.plazoleta.plazoleta.infrastructure.output.jpa.mapper.PedidoEntityMapper;
import com.plazoleta.plazoleta.infrastructure.output.jpa.repository.PedidoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

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
    public Optional<Pedido> obtenerPedidoPorId(Long idPedido) {
        return pedidoRepository.findById(idPedido).map(pedidoEntityMapper::toModel);
    }

    @Override
    public boolean existePedidoDeClienteEnEstados(Long idCliente, List<EstadoPedido> estados) {
        return pedidoRepository.existsByIdClienteAndEstadoIn(idCliente, estados);
    }

    @Override
    public PaginaRespuesta<Pedido> listarPedidosPorRestauranteYEstado(Long idRestaurante, EstadoPedido estado,
                                                                      int pagina, int tamanio) {
        Pageable pageable = PageRequest.of(pagina, tamanio,
                Sort.by(Sort.Order.asc("fecha"), Sort.Order.asc("id")));
        Page<PedidoEntity> resultado = pedidoRepository.findByIdRestauranteAndEstado(
                idRestaurante, estado, pageable);

        List<Pedido> contenido = resultado.getContent().stream()
                .map(pedidoEntityMapper::toModel)
                .toList();

        return new PaginaRespuesta<>(contenido, resultado.getNumber(), resultado.getSize(),
                resultado.getTotalElements(), resultado.getTotalPages());
    }
}