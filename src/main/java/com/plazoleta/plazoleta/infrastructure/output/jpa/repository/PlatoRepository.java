package com.plazoleta.plazoleta.infrastructure.output.jpa.repository;

import com.plazoleta.plazoleta.infrastructure.output.jpa.entity.PlatoEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlatoRepository extends JpaRepository<PlatoEntity, Long> {

    Page<PlatoEntity> findByIdRestauranteAndActivoTrue(Long idRestaurante, Pageable pageable);

    Page<PlatoEntity> findByIdRestauranteAndActivoTrueAndCategoriaIgnoreCase(
            Long idRestaurante, String categoria, Pageable pageable);
}