package com.plazoleta.plazoleta.infrastructure.input.rest.dto;

/**
 * Cuerpo del POST /api/v1/restaurantes.
 * No incluye id: lo genera la base de datos.
 */
public record RestauranteRequest(
        String nombre,
        String nit,
        String direccion,
        String telefono,
        String urlLogo,
        Long idPropietario) {
}