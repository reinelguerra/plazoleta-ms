package com.plazoleta.plazoleta.infrastructure.output.jpa.adapter;

import com.plazoleta.plazoleta.domain.model.Plato;
import com.plazoleta.plazoleta.infrastructure.output.jpa.entity.PlatoEntity;
import com.plazoleta.plazoleta.infrastructure.output.jpa.mapper.PlatoEntityMapper;
import com.plazoleta.plazoleta.infrastructure.output.jpa.repository.PlatoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlatoJpaAdapterObtenerTest {

    @Mock
    private PlatoRepository platoRepository;

    @Mock
    private PlatoEntityMapper platoEntityMapper;

    @InjectMocks
    private PlatoJpaAdapter adapter;

    @Test
    void obtenerPlatoPorId_existente_devuelveElModelo() {
        PlatoEntity entidad = new PlatoEntity();
        Plato modelo = new Plato(5L, "Bandeja", 25000, "Plato típico", "https://img.com/b.png", "Fuerte", true, 10L);
        when(platoRepository.findById(5L)).thenReturn(Optional.of(entidad));
        when(platoEntityMapper.toModel(entidad)).thenReturn(modelo);

        Optional<Plato> resultado = adapter.obtenerPlatoPorId(5L);

        assertTrue(resultado.isPresent());
        assertEquals(5L, resultado.get().getId());
    }

    @Test
    void obtenerPlatoPorId_inexistente_devuelveVacio() {
        when(platoRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Plato> resultado = adapter.obtenerPlatoPorId(99L);

        assertTrue(resultado.isEmpty());
        verifyNoInteractions(platoEntityMapper);
    }
}