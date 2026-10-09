package com.plazoleta.plazoleta.infrastructure.output.jpa.adapter;

import com.plazoleta.plazoleta.domain.model.Pagina;
import com.plazoleta.plazoleta.domain.model.Restaurante;
import com.plazoleta.plazoleta.infrastructure.output.jpa.entity.RestauranteEntity;
import com.plazoleta.plazoleta.infrastructure.output.jpa.mapper.RestauranteEntityMapper;
import com.plazoleta.plazoleta.infrastructure.output.jpa.repository.RestauranteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RestaurantePersistenceAdapterListadoTest {

    @Mock
    private RestauranteRepository restauranteRepository;

    @Mock
    private RestauranteEntityMapper restauranteEntityMapper;

    @InjectMocks
    private RestaurantePersistenceAdapter adapter;

    private Restaurante restaurante(String nombre) {
        return new Restaurante(1L, nombre, "900123456", "Calle 10 # 5-20",
                "+573001234567", "https://logo.com/" + nombre + ".png", 1L);
    }

    @Test
    void listarRestaurantes_pideLaPaginaOrdenadaPorNombreSinImportarMayusculas() {
        Page<RestauranteEntity> vacia = new PageImpl<>(List.of(), PageRequest.of(2, 5), 0);
        when(restauranteRepository.findAll(any(Pageable.class))).thenReturn(vacia);

        adapter.listarRestaurantes(2, 5);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(restauranteRepository).findAll(captor.capture());
        Pageable pedido = captor.getValue();
        assertEquals(2, pedido.getPageNumber());
        assertEquals(5, pedido.getPageSize());

        Sort.Order orden = pedido.getSort().getOrderFor("nombre");
        assertNotNull(orden);
        assertTrue(orden.isAscending());
        assertTrue(orden.isIgnoreCase());
    }

    @Test
    void listarRestaurantes_conviertePaginaYContenidoAlModeloDeDominio() {
        RestauranteEntity e1 = new RestauranteEntity();
        RestauranteEntity e2 = new RestauranteEntity();
        Page<RestauranteEntity> pagina = new PageImpl<>(List.of(e1, e2), PageRequest.of(0, 10), 12);
        when(restauranteRepository.findAll(any(Pageable.class))).thenReturn(pagina);
        when(restauranteEntityMapper.toModel(e1)).thenReturn(restaurante("Alfa"));
        when(restauranteEntityMapper.toModel(e2)).thenReturn(restaurante("Beta"));

        Pagina<Restaurante> resultado = adapter.listarRestaurantes(0, 10);

        assertEquals(2, resultado.contenido().size());
        assertEquals("Alfa", resultado.contenido().get(0).getNombre());
        assertEquals("Beta", resultado.contenido().get(1).getNombre());
        assertEquals(0, resultado.pagina());
        assertEquals(10, resultado.tamanio());
        assertEquals(12, resultado.totalElementos());
        assertEquals(2, resultado.totalPaginas());
    }
}