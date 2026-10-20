package com.plazoleta.plazoleta.infrastructure.input.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Restaurante creado")
public record RestauranteResponse(
        @Schema(description = "Identificador generado", example = "1") Long id,
        @Schema(description = "Nombre del restaurante", example = "Sabor 24") String nombre,
        @Schema(description = "NIT", example = "900123456") String nit,
        @Schema(description = "Dirección", example = "Calle 10 # 5-20") String direccion,
        @Schema(description = "Teléfono", example = "+573001234567") String telefono,
        @Schema(description = "URL del logo", example = "https://logo.com/sabor.png") String urlLogo,
        @Schema(description = "Id del propietario", example = "2") Long idPropietario) {
}