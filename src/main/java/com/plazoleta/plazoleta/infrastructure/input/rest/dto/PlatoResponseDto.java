package com.plazoleta.plazoleta.infrastructure.input.rest.dto;

public record PlatoResponseDto(
        Long id,
        String nombre,
        Integer precio,
        String descripcion,
        String urlImagen,
        String categoria,
        Boolean activo,
        Long idRestaurante) {
}