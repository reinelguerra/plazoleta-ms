package com.plazoleta.plazoleta.infrastructure.input.rest.controller;

import com.plazoleta.plazoleta.application.handler.PlatoHandler;
import com.plazoleta.plazoleta.domain.model.PaginaRespuesta;
import com.plazoleta.plazoleta.domain.model.Plato;
import com.plazoleta.plazoleta.infrastructure.exception.ErrorResponse;
import com.plazoleta.plazoleta.infrastructure.input.rest.dto.PlatoEstadoRequestDto;
import com.plazoleta.plazoleta.infrastructure.input.rest.dto.PlatoListadoResponse;
import com.plazoleta.plazoleta.infrastructure.input.rest.dto.PlatoModificacionRequestDto;
import com.plazoleta.plazoleta.infrastructure.input.rest.dto.PlatoRequestDto;
import com.plazoleta.plazoleta.infrastructure.input.rest.dto.PlatoResponseDto;
import com.plazoleta.plazoleta.infrastructure.input.rest.mapper.PlatoRestMapper;
import com.plazoleta.plazoleta.infrastructure.security.UsuarioAutenticado;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static com.plazoleta.plazoleta.infrastructure.input.rest.DocumentacionApi.DATOS_INVALIDOS;
import static com.plazoleta.plazoleta.infrastructure.input.rest.DocumentacionApi.NO_AUTENTICADO;
import static com.plazoleta.plazoleta.infrastructure.input.rest.DocumentacionApi.PLATO_NO_ENCONTRADO;
import static com.plazoleta.plazoleta.infrastructure.input.rest.DocumentacionApi.SIN_PERMISO_PLATO;

@RestController
@RequestMapping("/api/v1/platos")
@Tag(name = "Platos", description = "Creación, administración y consulta de los platos del menú")
@ApiResponse(responseCode = "401", description = NO_AUTENTICADO,
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
public class PlatoRestController {

    private final PlatoHandler platoHandler;
    private final PlatoRestMapper platoRestMapper;

    public PlatoRestController(PlatoHandler platoHandler, PlatoRestMapper platoRestMapper) {
        this.platoHandler = platoHandler;
        this.platoRestMapper = platoRestMapper;
    }


    @Operation(summary = "Crear un plato",
            description = "Solo el PROPIETARIO del restaurante al que se asigna el plato. El plato nace activo. "
                    + "Reglas: nombre, descripción, URL de imagen y categoría son obligatorios y "
                    + "el precio es un número entero mayor a 0.")
    @ApiResponse(responseCode = "400", description = DATOS_INVALIDOS,
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = SIN_PERMISO_PLATO,
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public ResponseEntity<PlatoResponseDto> crearPlato(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @RequestBody PlatoRequestDto request) {
        Long idPropietario = usuario == null ? null : usuario.id();
        Plato creado = platoHandler.crearPlato(platoRestMapper.toModel(request), idPropietario);
        return ResponseEntity.status(HttpStatus.CREATED).body(platoRestMapper.toResponse(creado));
    }

    @Operation(summary = "Modificar un plato",
            description = "Solo el PROPIETARIO del restaurante del plato. Únicamente se pueden cambiar el "
                    + "precio (entero mayor a 0) y la descripción; el resto del plato no se toca.")
    @ApiResponse(responseCode = "400", description = DATOS_INVALIDOS,
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = SIN_PERMISO_PLATO,
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = PLATO_NO_ENCONTRADO,
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PatchMapping("/{id}")
    public ResponseEntity<PlatoResponseDto> modificarPlato(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @RequestBody PlatoModificacionRequestDto request) {
        Plato modificado = platoHandler.modificarPlato(
                id, platoRestMapper.precioEntero(request.precio()), request.descripcion(), usuario.id());
        return ResponseEntity.ok(platoRestMapper.toResponse(modificado));
    }

    @Operation(summary = "Habilitar o deshabilitar un plato",
            description = "Solo el PROPIETARIO del restaurante del plato. activo = true lo habilita y "
                    + "activo = false lo deshabilita. No se pueden tocar platos de otros restaurantes.")
    @ApiResponse(responseCode = "400", description = DATOS_INVALIDOS,
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = SIN_PERMISO_PLATO,
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = PLATO_NO_ENCONTRADO,
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PatchMapping("/{id}/estado")
    public ResponseEntity<PlatoResponseDto> cambiarEstadoPlato(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @RequestBody PlatoEstadoRequestDto request) {
        Plato actualizado = platoHandler.cambiarEstadoPlato(id, request.activo(), usuario.id());
        return ResponseEntity.ok(platoRestMapper.toResponse(actualizado));
    }

    @Operation(summary = "Listar los platos de un restaurante",
            description = "Solo el CLIENTE. Devuelve únicamente los platos activos del restaurante, "
                    + "ordenados alfabéticamente por nombre y paginados. Se puede filtrar por categoría.")
    @ApiResponse(responseCode = "400",
            description = "Paginación inválida: la página no puede ser negativa y el tamaño debe ser al menos 1",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "Sin permiso: el rol del token no es CLIENTE",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "No existe un restaurante con ese id",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @GetMapping("/restaurante/{idRestaurante}")
    public ResponseEntity<PaginaRespuesta<PlatoListadoResponse>> listarPlatos(
            @Parameter(description = "Id del restaurante", example = "1")
            @PathVariable("idRestaurante") Long idRestaurante,
            @Parameter(description = "Categoría para filtrar (opcional)", example = "Postre")
            @RequestParam(name = "categoria", required = false) String categoria,
            @Parameter(description = "Número de página, empezando en 0", example = "0")
            @RequestParam(name = "pagina", defaultValue = "0") int pagina,
            @Parameter(description = "Cantidad de platos por página (mínimo 1)", example = "10")
            @RequestParam(name = "tamanio", defaultValue = "10") int tamanio) {

        PaginaRespuesta<Plato> resultado = platoHandler.listarPlatos(idRestaurante, categoria, pagina, tamanio);

        return ResponseEntity.ok(resultado.map(platoRestMapper::toListadoResponse));
    }
}