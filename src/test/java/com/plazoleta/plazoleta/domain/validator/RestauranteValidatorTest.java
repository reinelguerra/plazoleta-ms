package com.plazoleta.plazoleta.domain.validator;

import com.plazoleta.plazoleta.domain.exception.DomainException;
import com.plazoleta.plazoleta.domain.model.Restaurante;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RestauranteValidatorTest {

    /** Un restaurante válido; cada test le rompe UN solo campo. */
    private Restaurante restauranteValido() {
        return new Restaurante(null, "Sabor 24", "900123456", "Calle 10 # 5-20",
                "+573001234567", "https://logo.com/sabor.png", 1L);
    }

    private void assertMensaje(Restaurante r, String esperado) {
        DomainException ex = assertThrows(DomainException.class, () -> RestauranteValidator.validar(r));
        assertEquals(esperado, ex.getMessage());
    }

    @Test
    void restauranteValido_noLanzaExcepcion() {
        assertDoesNotThrow(() -> RestauranteValidator.validar(restauranteValido()));
    }

    @Test
    void nombreSoloNumeros_lanzaExcepcion() {
        Restaurante r = restauranteValido();
        r.setNombre("12345");
        assertMensaje(r, "El nombre del restaurante no puede contener solo números");
    }

    @Test
    void nombreConNumerosYLetras_esValido() {
        Restaurante r = restauranteValido();
        r.setNombre("Burger 99");
        assertDoesNotThrow(() -> RestauranteValidator.validar(r));
    }

    @Test
    void nombreVacio_lanzaExcepcion() {
        Restaurante r = restauranteValido();
        r.setNombre("  ");
        assertMensaje(r, "El nombre del restaurante es obligatorio");
    }

    @Test
    void nitNoNumerico_lanzaExcepcion() {
        Restaurante r = restauranteValido();
        r.setNit("900-123");
        assertMensaje(r, "El NIT debe ser numérico");
    }

    @Test
    void telefonoConLetras_lanzaExcepcion() {
        Restaurante r = restauranteValido();
        r.setTelefono("300ABC");
        assertThrows(DomainException.class, () -> RestauranteValidator.validar(r));
    }

    @Test
    void telefonoMasDe13Caracteres_lanzaExcepcion() {
        Restaurante r = restauranteValido();
        r.setTelefono("+5730012345678");
        assertThrows(DomainException.class, () -> RestauranteValidator.validar(r));
    }

    @Test
    void direccionVacia_lanzaExcepcion() {
        Restaurante r = restauranteValido();
        r.setDireccion(null);
        assertMensaje(r, "La dirección es obligatoria");
    }

    @Test
    void urlLogoVacia_lanzaExcepcion() {
        Restaurante r = restauranteValido();
        r.setUrlLogo("");
        assertMensaje(r, "La URL del logo es obligatoria");
    }

    @Test
    void propietarioNulo_lanzaExcepcion() {
        Restaurante r = restauranteValido();
        r.setIdPropietario(null);
        assertMensaje(r, "El id del propietario es obligatorio");
    }
}