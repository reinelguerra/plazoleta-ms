package com.plazoleta.plazoleta.infrastructure.output.jpa.repository;

import com.plazoleta.plazoleta.infrastructure.output.jpa.entity.RestauranteEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data genera la implementación sola: save, findById, findAll, etc.
 * No hay que escribir ni una consulta.
 */
public interface RestauranteRepository extends JpaRepository<RestauranteEntity, Long> {
}