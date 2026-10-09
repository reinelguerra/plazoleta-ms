package com.plazoleta.plazoleta.infrastructure.input.rest.controller;

import com.plazoleta.plazoleta.domain.api.RestauranteServicePort;
import com.plazoleta.plazoleta.domain.model.Restaurante;
import com.plazoleta.plazoleta.infrastructure.input.rest.dto.RestauranteRequest;
import com.plazoleta.plazoleta.infrastructure.input.rest.dto.RestauranteResponse;
import com.plazoleta.plazoleta.infrastructure.input.rest.mapper.RestauranteRequestMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Adaptador de entrada REST: traduce HTTP a una llamada al puerto del dominio.
 * (La restricción "solo administrador" llega con la seguridad JWT, en HU 5.)
 */
@RestController
@RequestMapping("/api/v1/restaurantes")
public class RestauranteController {

    private final RestauranteServicePort restauranteServicePort;
    private final RestauranteRequestMapper restauranteRequestMapper;

    public RestauranteController(RestauranteServicePort restauranteServicePort,
                                 RestauranteRequestMapper restauranteRequestMapper) {
        this.restauranteServicePort = restauranteServicePort;
        this.restauranteRequestMapper = restauranteRequestMapper;
    }

    @PostMapping
    public ResponseEntity<RestauranteResponse> crearRestaurante(@RequestBody RestauranteRequest request) {
        Restaurante creado = restauranteServicePort
                .crearRestaurante(restauranteRequestMapper.toModel(request));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(restauranteRequestMapper.toResponse(creado));
    }
}