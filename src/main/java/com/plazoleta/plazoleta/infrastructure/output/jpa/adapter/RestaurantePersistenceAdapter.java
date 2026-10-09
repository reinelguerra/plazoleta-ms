package com.plazoleta.plazoleta.infrastructure.output.jpa.adapter;

import com.plazoleta.plazoleta.domain.model.Restaurante;
import com.plazoleta.plazoleta.domain.spi.RestaurantePersistencePort;
import com.plazoleta.plazoleta.infrastructure.output.jpa.entity.RestauranteEntity;
import com.plazoleta.plazoleta.infrastructure.output.jpa.mapper.RestauranteEntityMapper;
import com.plazoleta.plazoleta.infrastructure.output.jpa.repository.RestauranteRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Adaptador de salida: implementa el puerto del dominio usando JPA.
 * Es la única clase que sabe que detrás hay una base de datos.
 */
@Component
public class RestaurantePersistenceAdapter implements RestaurantePersistencePort {

    private final RestauranteRepository restauranteRepository;
    private final RestauranteEntityMapper restauranteEntityMapper;

    public RestaurantePersistenceAdapter(RestauranteRepository restauranteRepository,
                                         RestauranteEntityMapper restauranteEntityMapper) {
        this.restauranteRepository = restauranteRepository;
        this.restauranteEntityMapper = restauranteEntityMapper;
    }

    @Override
    public Restaurante guardarRestaurante(Restaurante restaurante) {
        RestauranteEntity entidad = restauranteEntityMapper.toEntity(restaurante);
        RestauranteEntity guardada = restauranteRepository.save(entidad);
        return restauranteEntityMapper.toModel(guardada);
    }

    @Override
    public Optional<Restaurante> obtenerRestaurantePorId(Long id) {
        return restauranteRepository.findById(id).map(restauranteEntityMapper::toModel);
    }
}