package com.plazoleta.plazoleta.application.usecase;

import com.plazoleta.plazoleta.domain.exception.DomainException;
import com.plazoleta.plazoleta.domain.exception.PropietarioInvalidoException;
import com.plazoleta.plazoleta.domain.exception.UsuarioNoEncontradoException;
import com.plazoleta.plazoleta.domain.model.Restaurante;
import com.plazoleta.plazoleta.domain.model.RolUsuario;
import com.plazoleta.plazoleta.domain.spi.RestaurantePersistencePort;
import com.plazoleta.plazoleta.domain.spi.UsuarioClientPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RestauranteUseCaseTest {

    @Mock
    private RestaurantePersistencePort restaurantePersistencePort;

    @Mock
    private UsuarioClientPort usuarioClientPort;

    @InjectMocks
    private RestauranteUseCase restauranteUseCase;

    private Restaurante restauranteValido() {
        return new Restaurante(null, "Sabor 24", "900123456", "Calle 10 # 5-20",
                "+573001234567", "https://logo.com/sabor.png", 1L);
    }

    @Test
    void propietarioValido_guardaYDevuelveElRestaurante() {
        Restaurante entrada = restauranteValido();
        Restaurante guardado = restauranteValido();
        guardado.setId(10L);

        when(usuarioClientPort.obtenerRolPorId(1L)).thenReturn(Optional.of(RolUsuario.PROPIETARIO));
        when(restaurantePersistencePort.guardarRestaurante(entrada)).thenReturn(guardado);

        Restaurante resultado = restauranteUseCase.crearRestaurante(entrada);

        assertEquals(10L, resultado.getId());
        verify(restaurantePersistencePort).guardarRestaurante(entrada);
    }

    @Test
    void usuarioSinRolPropietario_lanzaExcepcionYNoGuarda() {
        when(usuarioClientPort.obtenerRolPorId(1L)).thenReturn(Optional.of(RolUsuario.CLIENTE));

        assertThrows(PropietarioInvalidoException.class,
                () -> restauranteUseCase.crearRestaurante(restauranteValido()));

        verify(restaurantePersistencePort, never()).guardarRestaurante(any());
    }

    @Test
    void usuarioInexistente_lanzaExcepcionYNoGuarda() {
        when(usuarioClientPort.obtenerRolPorId(1L)).thenReturn(Optional.empty());

        assertThrows(UsuarioNoEncontradoException.class,
                () -> restauranteUseCase.crearRestaurante(restauranteValido()));

        verify(restaurantePersistencePort, never()).guardarRestaurante(any());
    }

    @Test
    void datosInvalidos_lanzaExcepcionSinConsultarNada() {
        Restaurante invalido = restauranteValido();
        invalido.setNombre("12345");

        assertThrows(DomainException.class, () -> restauranteUseCase.crearRestaurante(invalido));

        verifyNoInteractions(usuarioClientPort, restaurantePersistencePort);
    }
}