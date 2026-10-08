package com.plazoleta.plazoleta.domain.spi;

import com.plazoleta.plazoleta.domain.model.Pagina;
import com.plazoleta.plazoleta.domain.model.Restaurante;

import java.util.Optional;

/**
 * Puerto de salida: lo que la aplicación necesita para guardar y consultar restaurantes.
 * No sabe si detrás hay H2, MySQL o MongoDB. Eso lo decide el adaptador.
 */
public interface RestaurantePersistencePort {

    Restaurante guardarRestaurante(Restaurante restaurante);

    // Lo usará HU 3 (Reinel) para comprobar que el restaurante existe.
    Optional<Restaurante> obtenerRestaurantePorId(Long id);

    /** Debe devolver la página pedida, ordenada alfabéticamente por nombre (sin distinguir mayúsculas). */
    Pagina<Restaurante> listarRestaurantes(int pagina, int tamanio);
}