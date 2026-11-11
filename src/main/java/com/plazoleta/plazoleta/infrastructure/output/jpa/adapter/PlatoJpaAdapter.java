package com.plazoleta.plazoleta.infrastructure.output.jpa.adapter;

import com.plazoleta.plazoleta.domain.model.PaginaRespuesta;
import com.plazoleta.plazoleta.domain.model.Plato;
import com.plazoleta.plazoleta.domain.spi.PlatoPersistencePort;
import com.plazoleta.plazoleta.infrastructure.output.jpa.entity.PlatoEntity;
import com.plazoleta.plazoleta.infrastructure.output.jpa.mapper.PlatoEntityMapper;
import com.plazoleta.plazoleta.infrastructure.output.jpa.repository.PlatoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

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

    @Override
    public Optional<Plato> obtenerPlatoPorId(Long id) {
        return platoRepository.findById(id).map(platoEntityMapper::toModel);
    }

    @Override
    public PaginaRespuesta<Plato> listarPlatosActivosPorRestaurante(Long idRestaurante, String categoria,
                                                                    int pagina, int tamanio) {
        Pageable pageable = PageRequest.of(pagina, tamanio, Sort.by(Sort.Order.asc("nombre").ignoreCase()));

        Page<PlatoEntity> resultado = categoria == null
                ? platoRepository.findByIdRestauranteAndActivoTrue(idRestaurante, pageable)
                : platoRepository.findByIdRestauranteAndActivoTrueAndCategoriaIgnoreCase(
                        idRestaurante, categoria, pageable);

        List<Plato> contenido = resultado.getContent().stream()
                .map(platoEntityMapper::toModel)
                .toList();

        return new PaginaRespuesta<>(contenido, resultado.getNumber(), resultado.getSize(),
                resultado.getTotalElements(), resultado.getTotalPages());
    }
}