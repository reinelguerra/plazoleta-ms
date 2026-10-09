package com.plazoleta.plazoleta.infrastructure.input.rest;

import com.plazoleta.plazoleta.domain.api.PlatoServicePort;
import com.plazoleta.plazoleta.domain.model.Plato;
import com.plazoleta.plazoleta.infrastructure.input.rest.dto.PlatoRequestDto;
import com.plazoleta.plazoleta.infrastructure.input.rest.dto.PlatoResponseDto;
import com.plazoleta.plazoleta.infrastructure.input.rest.mapper.PlatoRestMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/platos")
public class PlatoRestController {

    private final PlatoServicePort platoServicePort;
    private final PlatoRestMapper platoRestMapper;

    public PlatoRestController(PlatoServicePort platoServicePort, PlatoRestMapper platoRestMapper) {
        this.platoServicePort = platoServicePort;
        this.platoRestMapper = platoRestMapper;
    }

    @PostMapping
    public ResponseEntity<PlatoResponseDto> crearPlato(@RequestBody PlatoRequestDto request) {
        Plato creado = platoServicePort.crearPlato(platoRestMapper.toModel(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(platoRestMapper.toResponse(creado));
    }
}