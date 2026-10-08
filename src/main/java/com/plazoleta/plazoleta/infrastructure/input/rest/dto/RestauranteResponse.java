package com.plazoleta.plazoleta.infrastructure.input.rest.dto;

/** Lo que devuelve la API al crear un restaurante. */
public record RestauranteResponse(
        Long id,
        String nombre,
        String nit,
        String direccion,
        String telefono,
        String urlLogo,
        Long idPropietario) {
}