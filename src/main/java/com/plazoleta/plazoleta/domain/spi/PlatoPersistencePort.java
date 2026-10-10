package com.plazoleta.plazoleta.domain.spi;

import com.plazoleta.plazoleta.domain.model.Plato;

import java.util.Optional;

/**
 * Puerto de salida: lo que la aplicación necesita para guardar y consultar platos.
 * No sabe si detrás hay H2, MySQL o MongoDB. Eso lo decide el adaptador.
 */
public interface PlatoPersistencePort {

    Plato guardarPlato(Plato plato);

    Optional<Plato> obtenerPlatoPorId(Long id);
}