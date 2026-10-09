package com.plazoleta.plazoleta.application.usecase;

import com.plazoleta.plazoleta.domain.exception.DomainException;
import com.plazoleta.plazoleta.domain.exception.RestauranteNoEncontradoException;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlatoUseCaseTest {

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

    private void restauranteExiste() {
        when(restaurantePersistencePort.obtenerRestaurantePorId(1L))
                .thenReturn(Optional.of(new Restaurante()));
    }

    private void assertValidacion(String mensajeEsperado, Plato plato) {
        DomainException excepcion = assertThrows(DomainException.class,
                () -> platoUseCase.crearPlato(plato));

        assertEquals(mensajeEsperado, excepcion.getMessage());
        verifyNoInteractions(platoPersistencePort, restaurantePersistencePort);
    }

    @Test
    void crearPlato_conDatosValidos_guardaPlatoActivo() {
        restauranteExiste();
        when(platoPersistencePort.guardarPlato(any(Plato.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));

        Plato resultado = platoUseCase.crearPlato(platoValido());

        assertTrue(resultado.getActivo());
        verify(platoPersistencePort).guardarPlato(resultado);
    }

    @Test
    void crearPlato_aunqueVengaDesactivado_loGuardaActivo() {
        Plato plato = platoValido();
        plato.setActivo(false);
        restauranteExiste();
        when(platoPersistencePort.guardarPlato(any(Plato.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));

        Plato resultado = platoUseCase.crearPlato(plato);

        assertTrue(resultado.getActivo());
    }

    @Test
    void crearPlato_restauranteInexistente_lanzaNoEncontradoYNoGuarda() {
        when(restaurantePersistencePort.obtenerRestaurantePorId(1L)).thenReturn(Optional.empty());

        RestauranteNoEncontradoException excepcion = assertThrows(RestauranteNoEncontradoException.class,
                () -> platoUseCase.crearPlato(platoValido()));

        assertEquals("No existe un restaurante con id 1", excepcion.getMessage());
        verify(platoPersistencePort, never()).guardarPlato(any(Plato.class));
    }

    @Test
    void crearPlato_platoNulo_lanzaDomainException() {
        assertValidacion("Los datos del plato son obligatorios", null);
    }

    @Test
    void crearPlato_sinNombre_lanzaDomainException() {
        Plato plato = platoValido();
        plato.setNombre(" ");
        assertValidacion("El nombre del plato es obligatorio", plato);
    }

    @Test
    void crearPlato_precioCero_lanzaDomainException() {
        Plato plato = platoValido();
        plato.setPrecio(0);
        assertValidacion("El precio es obligatorio y debe ser un número entero mayor a 0", plato);
    }

    @Test
    void crearPlato_precioNegativo_lanzaDomainException() {
        Plato plato = platoValido();
        plato.setPrecio(-500);
        assertValidacion("El precio es obligatorio y debe ser un número entero mayor a 0", plato);
    }

    @Test
    void crearPlato_sinPrecio_lanzaDomainException() {
        Plato plato = platoValido();
        plato.setPrecio(null);
        assertValidacion("El precio es obligatorio y debe ser un número entero mayor a 0", plato);
    }

    @Test
    void crearPlato_sinDescripcion_lanzaDomainException() {
        Plato plato = platoValido();
        plato.setDescripcion(null);
        assertValidacion("La descripción es obligatoria", plato);
    }

    @Test
    void crearPlato_sinUrlImagen_lanzaDomainException() {
        Plato plato = platoValido();
        plato.setUrlImagen("");
        assertValidacion("La URL de la imagen es obligatoria", plato);
    }

    @Test
    void crearPlato_sinCategoria_lanzaDomainException() {
        Plato plato = platoValido();
        plato.setCategoria(null);
        assertValidacion("La categoría es obligatoria", plato);
    }

    @Test
    void crearPlato_sinRestaurante_lanzaDomainException() {
        Plato plato = platoValido();
        plato.setIdRestaurante(null);
        assertValidacion("El restaurante del plato es obligatorio", plato);
    }
}