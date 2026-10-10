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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlatoUseCaseModificarTest {

    @Mock
    private PlatoPersistencePort platoPersistencePort;

    @Mock
    private RestaurantePersistencePort restaurantePersistencePort;

    @InjectMocks
    private PlatoUseCase platoUseCase;

    private Plato platoExistente() {
        return new Plato(5L, "Bandeja", 25000, "Plato típico", "https://img.com/b.png", "Fuerte", true, 10L);
    }

    private Restaurante restauranteDe(Long idPropietario) {
        return new Restaurante(10L, "Sabor 24", "900123456", "Calle 10 # 5-20",
                "+573001234567", "https://logo.com/sabor.png", idPropietario);
    }

    @Test
    void propietarioDelRestaurante_modificaSoloPrecioYDescripcion() {
        when(platoPersistencePort.obtenerPlatoPorId(5L)).thenReturn(Optional.of(platoExistente()));
        when(restaurantePersistencePort.obtenerRestaurantePorId(10L)).thenReturn(Optional.of(restauranteDe(7L)));
        when(platoPersistencePort.guardarPlato(any(Plato.class))).thenAnswer(inv -> inv.getArgument(0));

        platoUseCase.modificarPlato(5L, 30000, "Nueva descripcion", 7L);

        ArgumentCaptor<Plato> captor = ArgumentCaptor.forClass(Plato.class);
        verify(platoPersistencePort).guardarPlato(captor.capture());
        Plato guardado = captor.getValue();
        assertEquals(Integer.valueOf(30000), guardado.getPrecio());
        assertEquals("Nueva descripcion", guardado.getDescripcion());
        // Lo demás no cambia
        assertEquals("Bandeja", guardado.getNombre());
        assertEquals("Fuerte", guardado.getCategoria());
        assertEquals("https://img.com/b.png", guardado.getUrlImagen());
        assertEquals(10L, guardado.getIdRestaurante());
        assertTrue(guardado.getActivo());
    }

    @Test
    void propietarioDeOtroRestaurante_lanzaExcepcionYNoGuarda() {
        when(platoPersistencePort.obtenerPlatoPorId(5L)).thenReturn(Optional.of(platoExistente()));
        when(restaurantePersistencePort.obtenerRestaurantePorId(10L)).thenReturn(Optional.of(restauranteDe(7L)));

        assertThrows(PropietarioNoAutorizadoException.class,
                () -> platoUseCase.modificarPlato(5L, 30000, "Nueva descripcion", 8L));

        verify(platoPersistencePort, never()).guardarPlato(any());
    }

    @Test
    void platoInexistente_lanzaExcepcionYNoGuarda() {
        when(platoPersistencePort.obtenerPlatoPorId(99L)).thenReturn(Optional.empty());

        assertThrows(PlatoNoEncontradoException.class,
                () -> platoUseCase.modificarPlato(99L, 30000, "Nueva descripcion", 7L));

        verify(platoPersistencePort, never()).guardarPlato(any());
    }

    @Test
    void precioNulo_lanzaExcepcionSinConsultarNada() {
        assertThrows(DomainException.class,
                () -> platoUseCase.modificarPlato(5L, null, "Nueva descripcion", 7L));

        verifyNoInteractions(platoPersistencePort, restaurantePersistencePort);
    }

    @Test
    void precioCero_lanzaExcepcionSinConsultarNada() {
        assertThrows(DomainException.class,
                () -> platoUseCase.modificarPlato(5L, 0, "Nueva descripcion", 7L));

        verifyNoInteractions(platoPersistencePort, restaurantePersistencePort);
    }

    @Test
    void descripcionVacia_lanzaExcepcionSinConsultarNada() {
        assertThrows(DomainException.class,
                () -> platoUseCase.modificarPlato(5L, 30000, "   ", 7L));

        verifyNoInteractions(platoPersistencePort, restaurantePersistencePort);
    }
}