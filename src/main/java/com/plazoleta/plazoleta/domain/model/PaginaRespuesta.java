package com.plazoleta.plazoleta.domain.model;

import java.util.List;
import java.util.function.Function;

/**
 * Resultado paginado propio. Nunca se devuelve Page ni Pageable de Spring.
 * La conversión desde Pageable se hace solo en el adaptador de persistencia.
 */
public record PaginaRespuesta<T>(List<T> contenido, int pagina, int tamanio,
                                 long totalElementos, int totalPaginas) {

    /** Convierte el contenido (por ejemplo, de modelo a DTO) conservando los datos de paginación. */
    public <R> PaginaRespuesta<R> map(Function<? super T, ? extends R> convertidor) {
        List<R> convertido = contenido.stream().<R>map(convertidor).toList();
        return new PaginaRespuesta<>(convertido, pagina, tamanio, totalElementos, totalPaginas);
    }
}
