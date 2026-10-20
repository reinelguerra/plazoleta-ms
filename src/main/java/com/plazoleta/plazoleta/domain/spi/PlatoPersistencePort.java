package com.plazoleta.plazoleta.domain.spi;

import com.plazoleta.plazoleta.domain.model.Plato;

import java.util.Optional;


public interface PlatoPersistencePort {

    Plato guardarPlato(Plato plato);

    Optional<Plato> obtenerPlatoPorId(Long id);
}