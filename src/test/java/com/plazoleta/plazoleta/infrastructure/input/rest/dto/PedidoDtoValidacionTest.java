package com.plazoleta.plazoleta.infrastructure.input.rest.dto;

import com.plazoleta.plazoleta.domain.util.MensajeError;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Las validaciones de formato del pedido viven en los DTOs de entrada (HU 11). */
class PedidoDtoValidacionTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    private Set<String> mensajes(Object dto) {
        return validator.validate(dto).stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toSet());
    }

    @Test
    void pedidoValido_sinErrores() {
        PedidoRequestDto dto = new PedidoRequestDto(1L, List.of(new PedidoPlatoRequestDto(10L, 2)));
        assertTrue(mensajes(dto).isEmpty());
    }

    @Test
    void sinRestaurante() {
        PedidoRequestDto dto = new PedidoRequestDto(null, List.of(new PedidoPlatoRequestDto(10L, 2)));
        assertEquals(Set.of(MensajeError.RESTAURANTE_OBLIGATORIO), mensajes(dto));
    }

    @Test
    void sinPlatos_nuloOVacio() {
        assertEquals(Set.of(MensajeError.PEDIDO_SIN_PLATOS), mensajes(new PedidoRequestDto(1L, null)));
        assertEquals(Set.of(MensajeError.PEDIDO_SIN_PLATOS), mensajes(new PedidoRequestDto(1L, List.of())));
    }

    @Test
    void platoSinId() {
        PedidoRequestDto dto = new PedidoRequestDto(1L, List.of(new PedidoPlatoRequestDto(null, 2)));
        assertEquals(Set.of(MensajeError.PLATO_PEDIDO_INVALIDO), mensajes(dto));
    }

    @Test
    void cantidadNulaCeroONegativa() {
        for (Integer cantidad : new Integer[]{null, 0, -3}) {
            PedidoRequestDto dto = new PedidoRequestDto(1L, List.of(new PedidoPlatoRequestDto(10L, cantidad)));
            assertEquals(Set.of(MensajeError.CANTIDAD_INVALIDA), mensajes(dto));
        }
    }
}