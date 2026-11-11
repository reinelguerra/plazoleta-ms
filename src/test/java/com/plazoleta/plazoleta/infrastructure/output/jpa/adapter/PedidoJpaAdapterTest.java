package com.plazoleta.plazoleta.infrastructure.output.jpa.adapter;

import com.plazoleta.plazoleta.domain.model.EstadoPedido;
import com.plazoleta.plazoleta.domain.model.Pedido;
import com.plazoleta.plazoleta.domain.model.PedidoPlato;
import com.plazoleta.plazoleta.infrastructure.output.jpa.entity.PedidoEntity;
import com.plazoleta.plazoleta.infrastructure.output.jpa.mapper.PedidoEntityMapper;
import com.plazoleta.plazoleta.infrastructure.output.jpa.repository.PedidoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PedidoJpaAdapterTest {

    private PedidoRepository repository;
    private PedidoJpaAdapter adapter;

    @BeforeEach
    void preparar() {
        repository = mock(PedidoRepository.class);
        adapter = new PedidoJpaAdapter(repository, Mappers.getMapper(PedidoEntityMapper.class));
    }

    @Test
    void guardarPedido_conviertePlatosYDevuelveElPedidoConSuId() {
        when(repository.save(any(PedidoEntity.class))).thenAnswer(invocacion -> {
            PedidoEntity entidad = invocacion.getArgument(0);
            entidad.setId(50L);
            return entidad;
        });

        Pedido pedido = new Pedido();
        pedido.setIdCliente(7L);
        pedido.setIdRestaurante(1L);
        pedido.setEstado(EstadoPedido.PENDIENTE);
        pedido.setFecha(LocalDateTime.of(2026, 11, 11, 15, 0));
        pedido.setPlatos(List.of(new PedidoPlato(10L, 2), new PedidoPlato(11L, 1)));

        Pedido guardado = adapter.guardarPedido(pedido);

        ArgumentCaptor<PedidoEntity> captor = ArgumentCaptor.forClass(PedidoEntity.class);
        verify(repository).save(captor.capture());
        assertEquals(2, captor.getValue().getPlatos().size());
        assertEquals(EstadoPedido.PENDIENTE, captor.getValue().getEstado());

        assertEquals(50L, guardado.getId());
        assertEquals(7L, guardado.getIdCliente());
        assertEquals(1L, guardado.getIdRestaurante());
        assertEquals(EstadoPedido.PENDIENTE, guardado.getEstado());
        assertEquals(2, guardado.getPlatos().size());
        assertEquals(10L, guardado.getPlatos().get(0).getIdPlato());
        assertEquals(2, guardado.getPlatos().get(0).getCantidad());
    }

    @Test
    void existePedidoDeClienteEnEstados_delegaEnElRepositorio() {
        when(repository.existsByIdClienteAndEstadoIn(7L, EstadoPedido.EN_PROCESO)).thenReturn(true);
        when(repository.existsByIdClienteAndEstadoIn(8L, EstadoPedido.EN_PROCESO)).thenReturn(false);

        assertTrue(adapter.existePedidoDeClienteEnEstados(7L, EstadoPedido.EN_PROCESO));
        assertFalse(adapter.existePedidoDeClienteEnEstados(8L, EstadoPedido.EN_PROCESO));
    }
}