package com.plazoleta.plazoleta.application.usecase;

import com.plazoleta.plazoleta.domain.exception.RestauranteNoEncontradoException;
import com.plazoleta.plazoleta.domain.model.Restaurante;
import com.plazoleta.plazoleta.domain.spi.RestaurantePersistencePort;
import com.plazoleta.plazoleta.domain.spi.UsuarioClientPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RestauranteUseCaseObtenerTest {

    @Mock
    private RestaurantePersistencePort restaurantePersistencePort;

    @Mock
    private UsuarioClientPort usuarioClientPort;

    @InjectMocks
    private RestauranteUseCase restauranteUseCase;

    @Test
    void restauranteExistente_loDevuelveConSuPropietario() {
        Restaurante restaurante = new Restaurante(1L, "Sabor 24", "900123456", "Calle 10 # 5-20",
                "+573001234567", "https://logo.com/s.png", 10L);
        when(restaurantePersistencePort.obtenerRestaurantePorId(1L)).thenReturn(Optional.of(restaurante));

        Restaurante resultado = restauranteUseCase.obtenerRestaurante(1L);

        assertSame(restaurante, resultado);
        assertEquals(10L, resultado.getIdPropietario());
    }

    @Test
    void restauranteInexistente_lanzaNoEncontrado() {
        when(restaurantePersistencePort.obtenerRestaurantePorId(99L)).thenReturn(Optional.empty());

        assertThrows(RestauranteNoEncontradoException.class, () -> restauranteUseCase.obtenerRestaurante(99L));
    }
}