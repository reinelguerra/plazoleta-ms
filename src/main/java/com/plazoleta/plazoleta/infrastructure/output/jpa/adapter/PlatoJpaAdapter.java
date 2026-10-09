package com.plazoleta.plazoleta.infrastructure.output.jpa.adapter;

import com.plazoleta.plazoleta.domain.model.Plato;
import com.plazoleta.plazoleta.domain.spi.PlatoPersistencePort;
import com.plazoleta.plazoleta.infrastructure.output.jpa.entity.PlatoEntity;
import com.plazoleta.plazoleta.infrastructure.output.jpa.mapper.PlatoEntityMapper;
import com.plazoleta.plazoleta.infrastructure.output.jpa.repository.PlatoRepository;

public class PlatoJpaAdapter implements PlatoPersistencePort {

    private final PlatoRepository platoRepository;
    private final PlatoEntityMapper platoEntityMapper;

    public PlatoJpaAdapter(PlatoRepository platoRepository, PlatoEntityMapper platoEntityMapper) {
        this.platoRepository = platoRepository;
        this.platoEntityMapper = platoEntityMapper;
    }

    @Override
    public Plato guardarPlato(Plato plato) {
        PlatoEntity guardado = platoRepository.save(platoEntityMapper.toEntity(plato));
        return platoEntityMapper.toModel(guardado);
    }
}