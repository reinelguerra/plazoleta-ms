package com.plazoleta.plazoleta.application.usecase;

import com.plazoleta.plazoleta.domain.exception.DomainException;
import com.plazoleta.plazoleta.domain.model.Plato;
import com.plazoleta.plazoleta.domain.spi.PlatoPersistencePort;
import com.plazoleta.plazoleta.domain.spi.RestaurantePersistencePort;
import com.plazoleta.plazoleta.domain.util.MensajeError;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;

/** Cada validación de platos responde con la constante centralizada de MensajeError. */
@ExtendWith(MockitoExtension.class)
class PlatoUseCaseMensajesTest {

    private static final Long ID_PROPIETARIO = 5L;

    @Mock
    private PlatoPersistencePort platoPersistencePort;

    @Mock
    private RestaurantePersistencePort restaurantePersistencePort;

    private PlatoUseCase platoUseCase;

    @BeforeEach
    void preparar() {
        platoUseCase = new PlatoUseCase(platoPersistencePort, restaurantePersistencePort);
    }

    private Plato platoValido() {
        Plato plato = new Plato();
        plato.setNombre("Bandeja paisa");
        plato.setPrecio(25000);
        plato.setDescripcion("Plato t\u00edpico");
        plato.setUrlImagen("http://img/bandeja.png");
        plato.setCategoria("Almuerzo");
        plato.setIdRestaurante(1L);
        return plato;
    }

    private void crearFalla(String mensajeEsperado, Consumer<Plato> dano) {
        Plato plato = platoValido();
        dano.accept(plato);
        DomainException e = assertThrows(DomainException.class,
                () -> platoUseCase.crearPlato(plato, ID_PROPIETARIO));
        assertEquals(mensajeEsperado, e.getMessage());
        verifyNoInteractions(platoPersistencePort, restaurantePersistencePort);
    }

    @Test
    void platoNulo() {
        DomainException e = assertThrows(DomainException.class,
                () -> platoUseCase.crearPlato(null, ID_PROPIETARIO));
        assertEquals(MensajeError.PLATO_OBLIGATORIO, e.getMessage());
    }

    @Test
    void nombreVacio() {
        crearFalla(MensajeError.NOMBRE_PLATO_OBLIGATORIO, p -> p.setNombre(" "));
    }

    @Test
    void precioNuloOCero() {
        crearFalla(MensajeError.PRECIO_PLATO_INVALIDO, p -> p.setPrecio(null));
        crearFalla(MensajeError.PRECIO_PLATO_INVALIDO, p -> p.setPrecio(0));
    }

    @Test
    void descripcionVacia() {
        crearFalla(MensajeError.DESCRIPCION_PLATO_OBLIGATORIA, p -> p.setDescripcion(""));
    }

    @Test
    void urlImagenVacia() {
        crearFalla(MensajeError.URL_IMAGEN_PLATO_OBLIGATORIA, p -> p.setUrlImagen(null));
    }

    @Test
    void categoriaVacia() {
        crearFalla(MensajeError.CATEGORIA_PLATO_OBLIGATORIA, p -> p.setCategoria(" "));
    }

    @Test
    void restauranteNulo() {
        crearFalla(MensajeError.RESTAURANTE_PLATO_OBLIGATORIO, p -> p.setIdRestaurante(null));
    }

    @Test
    void modificarConPrecioInvalido() {
        DomainException e = assertThrows(DomainException.class,
                () -> platoUseCase.modificarPlato(1L, 0, "algo", ID_PROPIETARIO));
        assertEquals(MensajeError.PRECIO_PLATO_INVALIDO, e.getMessage());
    }

    @Test
    void modificarConDescripcionVacia() {
        DomainException e = assertThrows(DomainException.class,
                () -> platoUseCase.modificarPlato(1L, 100, " ", ID_PROPIETARIO));
        assertEquals(MensajeError.DESCRIPCION_PLATO_OBLIGATORIA, e.getMessage());
    }

    @Test
    void cambiarEstadoSinEstado() {
        DomainException e = assertThrows(DomainException.class,
                () -> platoUseCase.cambiarEstadoPlato(1L, null, ID_PROPIETARIO));
        assertEquals(MensajeError.ESTADO_PLATO_OBLIGATORIO, e.getMessage());
    }
}