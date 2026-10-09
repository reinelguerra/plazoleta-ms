package com.plazoleta.plazoleta.infrastructure.input.rest.controller;

import com.plazoleta.plazoleta.domain.api.RestauranteServicePort;
import com.plazoleta.plazoleta.domain.model.Pagina;
import com.plazoleta.plazoleta.domain.model.Restaurante;
import com.plazoleta.plazoleta.infrastructure.input.rest.dto.PaginaResponse;
import com.plazoleta.plazoleta.infrastructure.input.rest.dto.RestauranteListadoResponse;
import com.plazoleta.plazoleta.infrastructure.input.rest.dto.RestauranteRequest;
import com.plazoleta.plazoleta.infrastructure.input.rest.dto.RestauranteResponse;
import com.plazoleta.plazoleta.infrastructure.input.rest.mapper.RestauranteRequestMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Adaptador de entrada REST: traduce HTTP a llamadas al puerto del dominio.
 * (Las restricciones por rol llegan con la seguridad JWT, en HU 5.)
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

    @GetMapping
    public ResponseEntity<PaginaResponse<RestauranteListadoResponse>> listarRestaurantes(
            @RequestParam(name = "pagina", defaultValue = "0") int pagina,
            @RequestParam(name = "tamanio", defaultValue = "10") int tamanio) {

        Pagina<Restaurante> resultado = restauranteServicePort.listarRestaurantes(pagina, tamanio);

        List<RestauranteListadoResponse> contenido = resultado.contenido().stream()
                .map(restauranteRequestMapper::toListadoResponse)
                .toList();

        return ResponseEntity.ok(new PaginaResponse<>(contenido, resultado.pagina(), resultado.tamanio(),
                resultado.totalElementos(), resultado.totalPaginas()));
    }
}