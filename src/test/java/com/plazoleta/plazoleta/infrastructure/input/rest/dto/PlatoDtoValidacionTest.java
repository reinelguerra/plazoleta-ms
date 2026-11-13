package com.plazoleta.plazoleta.infrastructure.input.rest.dto;

import com.plazoleta.plazoleta.domain.util.MensajeError;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Las validaciones de formato de los platos viven en los DTOs de entrada. */
class PlatoDtoValidacionTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    private Set<String> mensajes(Object dto) {
        return validator.validate(dto).stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toSet());
    }

    private PlatoRequestDto conPrecio(String precio) {
        return new PlatoRequestDto("Bandeja paisa", precio == null ? null : new BigDecimal(precio),
                "Frijoles y arroz", "http://img/bandeja.png", "Almuerzo", 1L);
    }

    @Test
    void crear_datosValidos_sinErrores() {
        assertTrue(mensajes(conPrecio("25000")).isEmpty());
    }

    @Test
    void crear_precioEnteroConDecimalesEnCero_esValido() {
        assertTrue(mensajes(conPrecio("25000.0")).isEmpty());
    }

    @Test
    void crear_nombreVacio() {
        PlatoRequestDto d = new PlatoRequestDto(" ", new BigDecimal("25000"), "d", "u", "c", 1L);
        assertEquals(Set.of(MensajeError.NOMBRE_PLATO_OBLIGATORIO), mensajes(d));
    }

    @Test
    void crear_precioNulo_cero_onegativo() {
        for (String precio : new String[]{null, "0", "-5"}) {
            assertEquals(Set.of(MensajeError.PRECIO_PLATO_INVALIDO), mensajes(conPrecio(precio)));
        }
    }

    @Test
    void crear_precioConDecimales_oMuyGrande_esNoEntero() {
        assertEquals(Set.of(MensajeError.PRECIO_PLATO_NO_ENTERO), mensajes(conPrecio("15.5")));
        assertEquals(Set.of(MensajeError.PRECIO_PLATO_NO_ENTERO), mensajes(conPrecio("3000000000")));
    }

    @Test
    void crear_descripcionUrlCategoriaYRestauranteObligatorios() {
        PlatoRequestDto d = new PlatoRequestDto("n", new BigDecimal("100"), "", " ", null, null);
        assertEquals(Set.of(MensajeError.DESCRIPCION_PLATO_OBLIGATORIA,
                MensajeError.URL_IMAGEN_PLATO_OBLIGATORIA,
                MensajeError.CATEGORIA_PLATO_OBLIGATORIA,
                MensajeError.RESTAURANTE_PLATO_OBLIGATORIO), mensajes(d));
    }

    @Test
    void modificar_validaPrecioYDescripcion() {
        assertTrue(mensajes(new PlatoModificacionRequestDto(new BigDecimal("100"), "algo")).isEmpty());
        assertEquals(Set.of(MensajeError.PRECIO_PLATO_INVALIDO),
                mensajes(new PlatoModificacionRequestDto(BigDecimal.ZERO, "algo")));
        assertEquals(Set.of(MensajeError.PRECIO_PLATO_NO_ENTERO),
                mensajes(new PlatoModificacionRequestDto(new BigDecimal("10.5"), "algo")));
        assertEquals(Set.of(MensajeError.DESCRIPCION_PLATO_OBLIGATORIA),
                mensajes(new PlatoModificacionRequestDto(new BigDecimal("100"), " ")));
    }

    @Test
    void estado_activoEsObligatorio() {
        assertTrue(mensajes(new PlatoEstadoRequestDto(false)).isEmpty());
        assertEquals(Set.of(MensajeError.ESTADO_PLATO_OBLIGATORIO),
                mensajes(new PlatoEstadoRequestDto(null)));
    }
}