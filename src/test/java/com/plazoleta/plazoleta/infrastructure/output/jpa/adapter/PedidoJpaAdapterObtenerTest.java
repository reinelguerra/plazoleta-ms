package com.plazoleta.plazoleta.infrastructure.output.jpa.adapter;

import com.plazoleta.plazoleta.domain.model.EstadoPedido;
import com.plazoleta.plazoleta.domain.model.Pedido;
import com.plazoleta.plazoleta.infrastructure.output.jpa.entity.PedidoEntity;
import com.plazoleta.plazoleta.infrastructure.output.jpa.entity.PedidoPlatoEntity;
import com.plazoleta.plazoleta.infrastructure.output.jpa.mapper.PedidoEntityMapper;
import com.plazoleta.plazoleta.infrastructure.output.jpa.repository.PedidoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PedidoJpaAdapterObtenerTest {

    private PedidoRepository repository;
    private PedidoJpaAdapter adapter;

    @BeforeEach
    void preparar() {
        repository = mock(PedidoRepository.class);
        adapter = new PedidoJpaAdapter(repository, Mappers.getMapper(PedidoEntityMapper.class));
    }

    @Test
    void existe_devuelveElPedidoConSusDatos() {
        PedidoEntity e = new PedidoEntity();
        e.setId(40L);
        e.setIdCliente(7L);
        e.setIdRestaurante(3L);
        e.setEstado(EstadoPedido.PENDIENTE);
        e.setFecha(LocalDateTime.of(2026, 11, 12, 10, 0));
        e.setPlatos(new ArrayList<>(List.of(new PedidoPlatoEntity(10L, 2))));
        when(repository.findById(40L)).thenReturn(Optional.of(e));

        Optional<Pedido> resultado = adapter.obtenerPedidoPorId(40L);

        assertTrue(resultado.isPresent());
        assertEquals(40L, resultado.get().getId());
        assertEquals(3L, resultado.get().getIdRestaurante());
        assertEquals(EstadoPedido.PENDIENTE, resultado.get().getEstado());
        assertEquals(1, resultado.get().getPlatos().size());
    }

    @Test
    void noExiste_devuelveVacio() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertTrue(adapter.obtenerPedidoPorId(99L).isEmpty());
    }
}