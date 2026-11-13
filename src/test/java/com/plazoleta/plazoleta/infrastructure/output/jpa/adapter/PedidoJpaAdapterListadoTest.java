package com.plazoleta.plazoleta.infrastructure.output.jpa.adapter;

import com.plazoleta.plazoleta.domain.model.EstadoPedido;
import com.plazoleta.plazoleta.domain.model.PaginaRespuesta;
import com.plazoleta.plazoleta.domain.model.Pedido;
import com.plazoleta.plazoleta.infrastructure.output.jpa.entity.PedidoEntity;
import com.plazoleta.plazoleta.infrastructure.output.jpa.entity.PedidoPlatoEntity;
import com.plazoleta.plazoleta.infrastructure.output.jpa.mapper.PedidoEntityMapper;
import com.plazoleta.plazoleta.infrastructure.output.jpa.repository.PedidoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PedidoJpaAdapterListadoTest {

    private PedidoRepository repository;
    private PedidoJpaAdapter adapter;

    @BeforeEach
    void preparar() {
        repository = mock(PedidoRepository.class);
        adapter = new PedidoJpaAdapter(repository, Mappers.getMapper(PedidoEntityMapper.class));
    }

    private PedidoEntity entidad(Long id) {
        PedidoEntity e = new PedidoEntity();
        e.setId(id);
        e.setIdCliente(7L);
        e.setIdRestaurante(3L);
        e.setEstado(EstadoPedido.PENDIENTE);
        e.setFecha(LocalDateTime.of(2026, 11, 12, 10, 0));
        e.setPlatos(new ArrayList<>(List.of(new PedidoPlatoEntity(10L, 2))));
        return e;
    }

    @Test
    void consultaPorRestauranteYEstado_ordenaPorFechaYConvierteLaPagina() {
        Page<PedidoEntity> page = new PageImpl<>(List.of(entidad(1L), entidad(2L)), PageRequest.of(0, 10), 2);
        when(repository.findByIdRestauranteAndEstado(eq(3L), eq(EstadoPedido.PENDIENTE), any(Pageable.class)))
                .thenReturn(page);

        PaginaRespuesta<Pedido> resultado = adapter.listarPedidosPorRestauranteYEstado(
                3L, EstadoPedido.PENDIENTE, 0, 10);

        assertEquals(2, resultado.contenido().size());
        assertEquals(1L, resultado.contenido().get(0).getId());
        assertEquals(EstadoPedido.PENDIENTE, resultado.contenido().get(0).getEstado());
        assertEquals(1, resultado.contenido().get(0).getPlatos().size());
        assertEquals(10L, resultado.contenido().get(0).getPlatos().get(0).getIdPlato());
        assertEquals(0, resultado.pagina());
        assertEquals(10, resultado.tamanio());
        assertEquals(2, resultado.totalElementos());
        assertEquals(1, resultado.totalPaginas());

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(repository).findByIdRestauranteAndEstado(eq(3L), eq(EstadoPedido.PENDIENTE), captor.capture());
        assertEquals(10, captor.getValue().getPageSize());
        assertNotNull(captor.getValue().getSort().getOrderFor("fecha"));
    }
}