package com.plazoleta.plazoleta.infrastructure.output.jpa.adapter;

import com.plazoleta.plazoleta.domain.model.Restaurante;
import com.plazoleta.plazoleta.infrastructure.output.jpa.entity.RestauranteEntity;
import com.plazoleta.plazoleta.infrastructure.output.jpa.mapper.RestauranteEntityMapper;
import com.plazoleta.plazoleta.infrastructure.output.jpa.repository.RestauranteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RestaurantePersistenceAdapterTest {

    @Mock
    private RestauranteRepository restauranteRepository;

    @Mock
    private RestauranteEntityMapper restauranteEntityMapper;

    @InjectMocks
    private RestaurantePersistenceAdapter adapter;

    private Restaurante restaurante(Long id) {
        return new Restaurante(id, "Sabor 24", "900123456", "Calle 10 # 5-20",
                "+573001234567", "https://logo.com/sabor.png", 1L);
    }

    @Test
    void guardarRestaurante_convierteGuardaYDevuelveElModelo() {
        Restaurante entrada = restaurante(null);
        Restaurante esperado = restaurante(10L);
        RestauranteEntity entidad = new RestauranteEntity();
        RestauranteEntity entidadGuardada = new RestauranteEntity();

        when(restauranteEntityMapper.toEntity(entrada)).thenReturn(entidad);
        when(restauranteRepository.save(entidad)).thenReturn(entidadGuardada);
        when(restauranteEntityMapper.toModel(entidadGuardada)).thenReturn(esperado);

        Restaurante resultado = adapter.guardarRestaurante(entrada);

        assertEquals(10L, resultado.getId());
    }

    @Test
    void obtenerRestaurantePorId_existente_devuelveElModelo() {
        RestauranteEntity entidad = new RestauranteEntity();

        when(restauranteRepository.findById(10L)).thenReturn(Optional.of(entidad));
        when(restauranteEntityMapper.toModel(entidad)).thenReturn(restaurante(10L));

        Optional<Restaurante> resultado = adapter.obtenerRestaurantePorId(10L);

        assertTrue(resultado.isPresent());
        assertEquals(10L, resultado.get().getId());
    }

    @Test
    void obtenerRestaurantePorId_inexistente_devuelveVacio() {
        when(restauranteRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Restaurante> resultado = adapter.obtenerRestaurantePorId(99L);

        assertTrue(resultado.isEmpty());
        verifyNoInteractions(restauranteEntityMapper);
    }
}