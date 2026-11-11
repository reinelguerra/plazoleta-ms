package com.plazoleta.plazoleta.infrastructure.input.rest.controller;

import com.plazoleta.plazoleta.application.handler.PedidoHandler;
import com.plazoleta.plazoleta.domain.model.Pedido;
import com.plazoleta.plazoleta.infrastructure.exception.ErrorResponse;
import com.plazoleta.plazoleta.infrastructure.input.rest.dto.PedidoRequestDto;
import com.plazoleta.plazoleta.infrastructure.input.rest.dto.PedidoResponseDto;
import com.plazoleta.plazoleta.infrastructure.input.rest.mapper.PedidoRestMapper;
import com.plazoleta.plazoleta.infrastructure.security.UsuarioAutenticado;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static com.plazoleta.plazoleta.infrastructure.input.rest.DocumentacionApi.DATOS_INVALIDOS;
import static com.plazoleta.plazoleta.infrastructure.input.rest.DocumentacionApi.NO_AUTENTICADO;

@RestController
@RequestMapping("/api/v1/pedidos")
@Tag(name = "Pedidos", description = "Realización y seguimiento de los pedidos")
@ApiResponse(responseCode = "401", description = NO_AUTENTICADO,
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
@ApiResponse(responseCode = "403", description = "Sin permiso: el rol del token no es CLIENTE",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
public class PedidoRestController {

    private final PedidoHandler pedidoHandler;
    private final PedidoRestMapper pedidoRestMapper;

    public PedidoRestController(PedidoHandler pedidoHandler, PedidoRestMapper pedidoRestMapper) {
        this.pedidoHandler = pedidoHandler;
        this.pedidoRestMapper = pedidoRestMapper;
    }

    @Operation(summary = "Realizar un pedido",
            description = "Solo el CLIENTE. El pedido es de un solo restaurante, con platos activos de ese "
                    + "restaurante y cantidades mayores a 0. Nace en estado PENDIENTE. Un cliente no puede "
                    + "hacer un nuevo pedido si tiene otro en proceso (PENDIENTE, EN_PREPARACION o LISTO).")
    @ApiResponse(responseCode = "400", description = DATOS_INVALIDOS,
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "No existe el restaurante o alguno de los platos",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public ResponseEntity<PedidoResponseDto> realizarPedido(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @Valid @RequestBody PedidoRequestDto request) {
        Pedido pedido = pedidoRestMapper.toModel(request);
        pedido.setIdCliente(usuario == null ? null : usuario.id());

        Pedido creado = pedidoHandler.realizarPedido(pedido);
        return ResponseEntity.status(HttpStatus.CREATED).body(pedidoRestMapper.toResponse(creado));
    }
}