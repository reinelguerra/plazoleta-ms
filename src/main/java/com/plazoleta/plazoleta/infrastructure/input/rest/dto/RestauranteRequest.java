package com.plazoleta.plazoleta.infrastructure.input.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Schema(description = "Datos para crear un restaurante")
public record RestauranteRequest(
        @Schema(description = "Nombre del restaurante. Puede incluir números, pero no estar formado solo por números",
                example = "Sabor 24", requiredMode = REQUIRED) String nombre,
        @Schema(description = "NIT: solo dígitos", example = "900123456",
                pattern = "^\\d+$", requiredMode = REQUIRED) String nit,
        @Schema(description = "Dirección del restaurante", example = "Calle 10 # 5-20",
                requiredMode = REQUIRED) String direccion,
        @Schema(description = "Teléfono: máximo 13 caracteres, solo dígitos y opcionalmente un + al inicio",
                example = "+573001234567", maxLength = 13, pattern = "^\\+?\\d{1,12}$",
                requiredMode = REQUIRED) String telefono,
        @Schema(description = "URL del logo", example = "https://logo.com/sabor.png",
                requiredMode = REQUIRED) String urlLogo,
        @Schema(description = "Id del propietario: debe existir en usuarios-ms con rol PROPIETARIO",
                example = "2", requiredMode = REQUIRED) Long idPropietario) {
}