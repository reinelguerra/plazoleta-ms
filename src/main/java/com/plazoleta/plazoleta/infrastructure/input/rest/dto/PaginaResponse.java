package com.plazoleta.plazoleta.infrastructure.input.rest.dto;

import java.util.List;

/** Envoltorio genérico para respuestas paginadas. */
public record PaginaResponse<T>(List<T> contenido, int pagina, int tamanio, long totalElementos, int totalPaginas) {
}