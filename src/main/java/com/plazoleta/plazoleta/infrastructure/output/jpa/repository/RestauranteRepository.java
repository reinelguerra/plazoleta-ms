package com.plazoleta.plazoleta.infrastructure.output.jpa.repository;

import com.plazoleta.plazoleta.infrastructure.output.jpa.entity.RestauranteEntity;
import org.springframework.data.jpa.repository.JpaRepository;


public interface RestauranteRepository extends JpaRepository<RestauranteEntity, Long> {
}