package com.plazoleta.plazoleta.domain.spi;

import com.plazoleta.plazoleta.domain.model.PaginaRespuesta;
import com.plazoleta.plazoleta.domain.model.Restaurante;

import java.util.Optional;


public interface RestaurantePersistencePort {

    Restaurante guardarRestaurante(Restaurante restaurante);

    Optional<Restaurante> obtenerRestaurantePorId(Long id);

    PaginaRespuesta<Restaurante> listarRestaurantes(int PaginaRespuesta, int tamanio);
}
