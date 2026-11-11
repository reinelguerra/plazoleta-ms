package com.plazoleta.plazoleta.infrastructure.input.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Plato en el menú de un restaurante (solo se listan los activos)")
public record PlatoListadoResponse(
        @Schema(description = "Identificador del plato", example = "1") Long id,
        @Schema(description = "Nombre del plato", example = "Bandeja paisa") String nombre,
        @Schema(description = "Precio (entero)", example = "25000") Integer precio,
        @Schema(description = "Descripción", example = "Frijoles, arroz, carne molida y chicharrón") String descripcion,
        @Schema(description = "URL de la imagen", example = "https://imagenes.com/bandeja.png") String urlImagen,
        @Schema(description = "Categoría", example = "Plato fuerte") String categoria) {
}