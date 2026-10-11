package com.plazoleta.plazoleta.application.usecase;

import com.plazoleta.plazoleta.domain.exception.PropietarioNoAutorizadoException;
import com.plazoleta.plazoleta.domain.model.Plato;
import com.plazoleta.plazoleta.domain.model.Restaurante;
import com.plazoleta.plazoleta.domain.spi.PlatoPersistencePort;
import com.plazoleta.plazoleta.domain.spi.RestaurantePersistencePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlatoUseCaseCrearAutorizacionTest {

    private static final Long ID_DUENO = 10L;
    private static final Long ID_OTRO = 99L;

    @Mock
    private PlatoPersistencePort platoPersistencePort;

    @Mock
    private RestaurantePersistencePort restaurantePersistencePort;

    private PlatoUseCase platoUseCase;

    @BeforeEach
    void setUp() {
        platoUseCase = new PlatoUseCase(platoPersistencePort, restaurantePersistencePort);
    }

    private Plato platoValido() {
        Plato plato = new Plato();
        plato.setNombre("Hamburguesa");
        plato.setPrecio(15000);
        plato.setDescripcion("Con queso y tocineta");
        plato.setUrlImagen("http://imagenes.com/hamburguesa.png");
        plato.setCategoria("PLATO_FUERTE");
        plato.setIdRestaurante(1L);
        return plato;
    }

    private void restauranteDelDueno() {
        Restaurante restaurante = new Restaurante();
        restaurante.setIdPropietario(ID_DUENO);
        when(restaurantePersistencePort.obtenerRestaurantePorId(1L)).thenReturn(Optional.of(restaurante));
    }

    @Test
    void crearPlato_elDuenoDelRestaurante_guardaElPlatoActivo() {
        restauranteDelDueno();
        when(platoPersistencePort.guardarPlato(any(Plato.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));

        Plato resultado = platoUseCase.crearPlato(platoValido(), ID_DUENO);

        assertTrue(resultado.getActivo());
        verify(platoPersistencePort).guardarPlato(resultado);
    }

    @Test
    void crearPlato_otroPropietario_lanzaNoAutorizadoYNoGuarda() {
        restauranteDelDueno();

        assertThrows(PropietarioNoAutorizadoException.class,
                () -> platoUseCase.crearPlato(platoValido(), ID_OTRO));

        verify(platoPersistencePort, never()).guardarPlato(any(Plato.class));
    }

    @Test
    void crearPlato_sinIdPropietario_lanzaNoAutorizadoYNoGuarda() {
        restauranteDelDueno();

        assertThrows(PropietarioNoAutorizadoException.class,
                () -> platoUseCase.crearPlato(platoValido(), null));

        verify(platoPersistencePort, never()).guardarPlato(any(Plato.class));
    }
}