package com.plazoleta.plazoleta.infrastructure.input.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/** HU 9: del restaurante solo se muestra el nombre y el logo. */
@Schema(description = "Restaurante en el listado: solo nombre y logo")
public record RestauranteListadoResponse(
        @Schema(description = "Nombre del restaurante", example = "Sabor 24") String nombre,
        @Schema(description = "URL del logo", example = "https://logo.com/sabor.png") String urlLogo) {
}