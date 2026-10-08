package com.plazoleta.plazoleta.domain.model;

import java.util.List;

/**
 * Resultado paginado del dominio. Es propio (no usa Page de Spring Data)
 * para que el núcleo no dependa de ningún framework.
 */
public record Pagina<T>(List<T> contenido, int pagina, int tamanio, long totalElementos, int totalPaginas) {
}