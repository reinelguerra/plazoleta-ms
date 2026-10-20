package com.plazoleta.plazoleta.infrastructure.input.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Resultado paginado")
public record PaginaResponse<T>(
        @Schema(description = "Elementos de la página actual") List<T> contenido,
        @Schema(description = "Número de la página actual, empezando en 0", example = "0") int pagina,
        @Schema(description = "Cantidad de elementos pedidos por página", example = "10") int tamanio,
        @Schema(description = "Total de elementos en todas las páginas", example = "25") long totalElementos,
        @Schema(description = "Total de páginas disponibles", example = "3") int totalPaginas) {
}