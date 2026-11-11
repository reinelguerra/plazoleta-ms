package com.plazoleta.plazoleta.infrastructure.output.jpa.adapter;

import com.plazoleta.plazoleta.domain.model.PaginaRespuesta;
import com.plazoleta.plazoleta.domain.model.Plato;
import com.plazoleta.plazoleta.infrastructure.output.jpa.entity.PlatoEntity;
import com.plazoleta.plazoleta.infrastructure.output.jpa.mapper.PlatoEntityMapper;
import com.plazoleta.plazoleta.infrastructure.output.jpa.repository.PlatoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PlatoJpaAdapterListadoTest {

    private PlatoRepository repository;
    private PlatoJpaAdapter adapter;

    @BeforeEach
    void preparar() {
        repository = mock(PlatoRepository.class);
        adapter = new PlatoJpaAdapter(repository, Mappers.getMapper(PlatoEntityMapper.class));
    }

    private PlatoEntity entidad(String nombre, String categoria) {
        PlatoEntity e = new PlatoEntity();
        e.setId(1L);
        e.setNombre(nombre);
        e.setPrecio(10000);
        e.setDescripcion("Descripcion");
        e.setUrlImagen("https://img.com/x.png");
        e.setCategoria(categoria);
        e.setActivo(true);
        e.setIdRestaurante(1L);
        return e;
    }

    @Test
    void sinCategoria_usaLaConsultaDeActivosYConvierteLaPagina() {
        Page<PlatoEntity> page = new PageImpl<>(
                List.of(entidad("Flan", "Postre"), entidad("Sopa", "Entrada")), PageRequest.of(0, 10), 2);
        when(repository.findByIdRestauranteAndActivoTrue(eq(1L), any(Pageable.class))).thenReturn(page);

        PaginaRespuesta<Plato> resultado = adapter.listarPlatosActivosPorRestaurante(1L, null, 0, 10);

        assertEquals(2, resultado.contenido().size());
        assertEquals("Flan", resultado.contenido().get(0).getNombre());
        assertEquals(0, resultado.pagina());
        assertEquals(10, resultado.tamanio());
        assertEquals(2, resultado.totalElementos());
        assertEquals(1, resultado.totalPaginas());
        verify(repository, never()).findByIdRestauranteAndActivoTrueAndCategoriaIgnoreCase(
                any(), any(), any());
    }

    @Test
    void conCategoria_usaLaConsultaFiltradaPorCategoria() {
        Page<PlatoEntity> page = new PageImpl<>(List.of(entidad("Flan", "Postre")), PageRequest.of(1, 5), 6);
        when(repository.findByIdRestauranteAndActivoTrueAndCategoriaIgnoreCase(
                eq(1L), eq("Postre"), any(Pageable.class))).thenReturn(page);

        PaginaRespuesta<Plato> resultado = adapter.listarPlatosActivosPorRestaurante(1L, "Postre", 1, 5);

        assertEquals(1, resultado.contenido().size());
        assertEquals(1, resultado.pagina());
        assertEquals(5, resultado.tamanio());
        assertEquals(6, resultado.totalElementos());
        assertEquals(2, resultado.totalPaginas());
        verify(repository, never()).findByIdRestauranteAndActivoTrue(any(), any());
    }
}