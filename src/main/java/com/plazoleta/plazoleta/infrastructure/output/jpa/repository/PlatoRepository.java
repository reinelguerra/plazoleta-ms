package com.plazoleta.plazoleta.infrastructure.output.jpa.repository;

import com.plazoleta.plazoleta.infrastructure.output.jpa.entity.PlatoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlatoRepository extends JpaRepository<PlatoEntity, Long> {
}