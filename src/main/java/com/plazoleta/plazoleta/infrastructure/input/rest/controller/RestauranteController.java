package com.plazoleta.plazoleta.infrastructure.input.rest.controller;

import com.plazoleta.plazoleta.domain.api.RestauranteServicePort;
import com.plazoleta.plazoleta.domain.model.Pagina;
import com.plazoleta.plazoleta.domain.model.Restaurante;
import com.plazoleta.plazoleta.infrastructure.exception.ErrorResponse;
import com.plazoleta.plazoleta.infrastructure.input.rest.dto.PaginaResponse;
import com.plazoleta.plazoleta.infrastructure.input.rest.dto.RestauranteListadoResponse;
import com.plazoleta.plazoleta.infrastructure.input.rest.dto.RestauranteRequest;
import com.plazoleta.plazoleta.infrastructure.input.rest.dto.RestauranteResponse;
import com.plazoleta.plazoleta.infrastructure.input.rest.mapper.RestauranteRequestMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.plazoleta.plazoleta.infrastructure.input.rest.DocumentacionApi.DATOS_INVALIDOS;
import static com.plazoleta.plazoleta.infrastructure.input.rest.DocumentacionApi.NO_AUTENTICADO;
import static com.plazoleta.plazoleta.infrastructure.input.rest.DocumentacionApi.SIN_PERMISO;
import static com.plazoleta.plazoleta.infrastructure.input.rest.DocumentacionApi.USUARIOS_NO_DISPONIBLE;


@RestController
@RequestMapping("/api/v1/restaurantes")
@Tag(name = "Restaurantes", description = "Creación y consulta de restaurantes")
@ApiResponse(responseCode = "401", description = NO_AUTENTICADO,
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
@ApiResponse(responseCode = "403", description = SIN_PERMISO,
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
public class RestauranteController {

    private final RestauranteServicePort restauranteServicePort;
    private final RestauranteRequestMapper restauranteRequestMapper;

    public RestauranteController(RestauranteServicePort restauranteServicePort,
                                 RestauranteRequestMapper restauranteRequestMapper) {
        this.restauranteServicePort = restauranteServicePort;
        this.restauranteRequestMapper = restauranteRequestMapper;
    }

    @Operation(summary = "Crear un restaurante",
            description = "Solo el ADMINISTRADOR. Reglas: el nombre no puede ser solo números, el NIT es "
                    + "numérico, el teléfono tiene máximo 13 caracteres (puede empezar con +) y el propietario "
                    + "debe existir en usuarios-ms con rol PROPIETARIO.")
    @ApiResponse(responseCode = "400", description = DATOS_INVALIDOS,
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "El propietario indicado no existe en usuarios-ms",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "503", description = USUARIOS_NO_DISPONIBLE,
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public ResponseEntity<RestauranteResponse> crearRestaurante(@RequestBody RestauranteRequest request) {
        Restaurante creado = restauranteServicePort
                .crearRestaurante(restauranteRequestMapper.toModel(request));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(restauranteRequestMapper.toResponse(creado));
    }

    @Operation(summary = "Listar restaurantes",
            description = "Solo el CLIENTE. Devuelve únicamente el nombre y el logo, ordenados "
                    + "alfabéticamente por nombre (sin distinguir mayúsculas) y paginados.")
    @ApiResponse(responseCode = "400",
            description = "Paginación inválida: la página no puede ser negativa y el tamaño debe ser al menos 1",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @GetMapping
    public ResponseEntity<PaginaResponse<RestauranteListadoResponse>> listarRestaurantes(
            @Parameter(description = "Número de página, empezando en 0", example = "0")
            @RequestParam(name = "pagina", defaultValue = "0") int pagina,
            @Parameter(description = "Cantidad de restaurantes por página (mínimo 1)", example = "10")
            @RequestParam(name = "tamanio", defaultValue = "10") int tamanio) {

        Pagina<Restaurante> resultado = restauranteServicePort.listarRestaurantes(pagina, tamanio);

        List<RestauranteListadoResponse> contenido = resultado.contenido().stream()
                .map(restauranteRequestMapper::toListadoResponse)
                .toList();

        return ResponseEntity.ok(new PaginaResponse<>(contenido, resultado.pagina(), resultado.tamanio(),
                resultado.totalElementos(), resultado.totalPaginas()));
    }
}