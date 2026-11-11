package com.plazoleta.plazoleta.domain.spi;

import com.plazoleta.plazoleta.domain.model.PaginaRespuesta;
import com.plazoleta.plazoleta.domain.model.Plato;

import java.util.Optional;


public interface PlatoPersistencePort {

    Plato guardarPlato(Plato plato);

    Optional<Plato> obtenerPlatoPorId(Long id);

    /** Solo platos activos del restaurante. Si categoria es null no se filtra por categoría. */
    PaginaRespuesta<Plato> listarPlatosActivosPorRestaurante(Long idRestaurante, String categoria,
                                                             int pagina, int tamanio);
}