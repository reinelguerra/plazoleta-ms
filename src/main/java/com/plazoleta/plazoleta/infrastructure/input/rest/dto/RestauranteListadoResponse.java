package com.plazoleta.plazoleta.infrastructure.input.rest.dto;

/** HU 9: del restaurante solo se muestra el nombre y el logo. */
public record RestauranteListadoResponse(String nombre, String urlLogo) {
}