package com.plazoleta.plazoleta.infrastructure.input.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Schema(description = "Datos para crear un plato")
public record PlatoRequestDto(
        @Schema(description = "Nombre del plato", example = "Bandeja paisa",
                requiredMode = REQUIRED) String nombre,
        @Schema(description = "Precio: número entero mayor a 0", type = "integer", example = "25000",
                minimum = "1", requiredMode = REQUIRED) BigDecimal precio,
        @Schema(description = "Descripción del plato", example = "Frijoles, arroz, carne molida y chicharrón",
                requiredMode = REQUIRED) String descripcion,
        @Schema(description = "URL de la imagen del plato", example = "https://imagenes.com/bandeja.png",
                requiredMode = REQUIRED) String urlImagen,
        @Schema(description = "Categoría del plato", example = "Plato fuerte",
                requiredMode = REQUIRED) String categoria,
        @Schema(description = "Id del restaurante al que pertenece: debe ser del propietario autenticado",
                example = "1", requiredMode = REQUIRED) Long idRestaurante) {
}