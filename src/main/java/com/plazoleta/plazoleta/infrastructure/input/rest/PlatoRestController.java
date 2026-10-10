package com.plazoleta.plazoleta.infrastructure.input.rest;

import com.plazoleta.plazoleta.domain.api.PlatoServicePort;
import com.plazoleta.plazoleta.domain.model.Plato;
import com.plazoleta.plazoleta.infrastructure.input.rest.dto.PlatoModificacionRequestDto;
import com.plazoleta.plazoleta.infrastructure.input.rest.dto.PlatoRequestDto;
import com.plazoleta.plazoleta.infrastructure.input.rest.dto.PlatoResponseDto;
import com.plazoleta.plazoleta.infrastructure.input.rest.mapper.PlatoRestMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.plazoleta.plazoleta.infrastructure.input.rest.dto.PlatoEstadoRequestDto;

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

    /**
     * HU 4: modifica solo precio y descripción.
     * PROVISIONAL: el id del propietario llega en una cabecera hasta que exista el JWT (HU 5).
     */
    @PatchMapping("/{id}")
    public ResponseEntity<PlatoResponseDto> modificarPlato(
            @PathVariable("id") Long id,
            @RequestHeader("X-Propietario-Id") Long idPropietario,
            @RequestBody PlatoModificacionRequestDto request) {
        Plato modificado = platoServicePort.modificarPlato(
                id, platoRestMapper.precioEntero(request.precio()), request.descripcion(), idPropietario);
        return ResponseEntity.ok(platoRestMapper.toResponse(modificado));
    }

        /**
     * HU 7: habilita o deshabilita un plato.
     * PROVISIONAL: el id del propietario llega en una cabecera hasta que exista el JWT (HU 5).
     */
    @PatchMapping("/{id}/estado")
    public ResponseEntity<PlatoResponseDto> cambiarEstadoPlato(
            @PathVariable("id") Long id,
            @RequestHeader("X-Propietario-Id") Long idPropietario,
            @RequestBody PlatoEstadoRequestDto request) {
        Plato actualizado = platoServicePort.cambiarEstadoPlato(id, request.activo(), idPropietario);
        return ResponseEntity.ok(platoRestMapper.toResponse(actualizado));
    }
}