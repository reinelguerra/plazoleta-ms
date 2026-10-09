package com.plazoleta.plazoleta.infrastructure.input.rest.dto;

import java.math.BigDecimal;

public record PlatoRequestDto(
        String nombre,
        BigDecimal precio,
        String descripcion,
        String urlImagen,
        String categoria,
        Long idRestaurante) {
}