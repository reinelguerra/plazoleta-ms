package com.plazoleta.plazoleta.domain.spi;

import com.plazoleta.plazoleta.domain.model.Restaurante;

/**
 * Puerto de salida: lo que la aplicación necesita para guardar restaurantes.
 * No sabe si detrás hay H2, MySQL o MongoDB. Eso lo decide el adaptador.
 */
public interface RestaurantePersistencePort {

    Restaurante guardarRestaurante(Restaurante restaurante);
}