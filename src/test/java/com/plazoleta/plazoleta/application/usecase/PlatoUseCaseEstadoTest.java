package com.plazoleta.plazoleta.application.usecase;

import com.plazoleta.plazoleta.domain.exception.DomainException;
import com.plazoleta.plazoleta.domain.exception.PlatoNoEncontradoException;
import com.plazoleta.plazoleta.domain.exception.PropietarioNoAutorizadoException;
import com.plazoleta.plazoleta.domain.model.Plato;
import com.plazoleta.plazoleta.domain.model.Restaurante;
import com.plazoleta.plazoleta.domain.spi.PlatoPersistencePort;
import com.plazoleta.plazoleta.domain.spi.RestaurantePersistencePort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlatoUseCaseEstadoTest {

    @Mock
    private PlatoPersistencePort platoPersistencePort;

    @Mock
    private RestaurantePersistencePort restaurantePersistencePort;

    @InjectMocks
    private PlatoUseCase platoUseCase;

    private Plato plato(boolean activo) {
        return new Plato(5L, "Bandeja", 25000, "Plato típico", "https://img.com/b.png", "Fuerte", activo, 10L);
    }

    private Restaurante restauranteDe(Long idPropietario) {
        return new Restaurante(10L, "Sabor 24", "900123456", "Calle 10 # 5-20",
                "+573001234567", "https://logo.com/sabor.png", idPropietario);
    }

    private void conPlatoYRestaurante(Plato plato, Long idPropietario) {
        when(platoPersistencePort.obtenerPlatoPorId(5L)).thenReturn(Optional.of(plato));
        when(restaurantePersistencePort.obtenerRestaurantePorId(10L))
                .thenReturn(Optional.of(restauranteDe(idPropietario)));
    }

    @Test
    void propietario_deshabilitaElPlato_yNoCambiaNadaMas() {
        conPlatoYRestaurante(plato(true), 7L);
        when(platoPersistencePort.guardarPlato(any(Plato.class))).thenAnswer(inv -> inv.getArgument(0));

        platoUseCase.cambiarEstadoPlato(5L, false, 7L);

        ArgumentCaptor<Plato> captor = ArgumentCaptor.forClass(Plato.class);
        verify(platoPersistencePort).guardarPlato(captor.capture());
        Plato guardado = captor.getValue();
        assertFalse(guardado.getActivo());
        assertEquals("Bandeja", guardado.getNombre());
        assertEquals(Integer.valueOf(25000), guardado.getPrecio());
        assertEquals("Plato típico", guardado.getDescripcion());
    }

    @Test
    void propietario_habilitaElPlato() {
        conPlatoYRestaurante(plato(false), 7L);
        when(platoPersistencePort.guardarPlato(any(Plato.class))).thenAnswer(inv -> inv.getArgument(0));

        Plato resultado = platoUseCase.cambiarEstadoPlato(5L, true, 7L);

        assertTrue(resultado.getActivo());
    }

    @Test
    void propietarioDeOtroRestaurante_lanzaExcepcionYNoGuarda() {
        conPlatoYRestaurante(plato(true), 7L);

        assertThrows(PropietarioNoAutorizadoException.class,
                () -> platoUseCase.cambiarEstadoPlato(5L, false, 8L));

        verify(platoPersistencePort, never()).guardarPlato(any());
    }

    @Test
    void platoInexistente_lanzaExcepcionYNoGuarda() {
        when(platoPersistencePort.obtenerPlatoPorId(99L)).thenReturn(Optional.empty());

        assertThrows(PlatoNoEncontradoException.class,
                () -> platoUseCase.cambiarEstadoPlato(99L, false, 7L));

        verify(platoPersistencePort, never()).guardarPlato(any());
    }

    @Test
    void estadoNulo_lanzaExcepcionSinConsultarNada() {
        assertThrows(DomainException.class,
                () -> platoUseCase.cambiarEstadoPlato(5L, null, 7L));

        verifyNoInteractions(platoPersistencePort, restaurantePersistencePort);
    }
}