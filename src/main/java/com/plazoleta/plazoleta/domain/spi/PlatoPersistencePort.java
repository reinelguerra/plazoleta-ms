package com.plazoleta.plazoleta.domain.spi;

import com.plazoleta.plazoleta.domain.model.Plato;

/**
 * Puerto de salida: lo que la aplicación necesita para guardar platos.
 * No sabe si detrás hay H2, MySQL o MongoDB. Eso lo decide el adaptador.
 */
public interface PlatoPersistencePort {

    Plato guardarPlato(Plato plato);
}