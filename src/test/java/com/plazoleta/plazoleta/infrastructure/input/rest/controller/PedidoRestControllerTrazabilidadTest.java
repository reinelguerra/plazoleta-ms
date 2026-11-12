package com.plazoleta.plazoleta.infrastructure.input.rest.controller;

import com.plazoleta.plazoleta.application.handler.PedidoHandler;
import com.plazoleta.plazoleta.domain.model.Pedido;
import com.plazoleta.plazoleta.domain.model.RolUsuario;
import com.plazoleta.plazoleta.domain.util.MensajeError;
import com.plazoleta.plazoleta.infrastructure.exception.GlobalExceptionHandler;
import com.plazoleta.plazoleta.infrastructure.exception.ServicioTrazabilidadNoDisponibleException;
import com.plazoleta.plazoleta.infrastructure.input.rest.mapper.PedidoRestMapper;
import com.plazoleta.plazoleta.infrastructure.security.UsuarioAutenticado;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PedidoRestControllerTrazabilidadTest {

    @AfterEach
    void limpiar() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void siTrazabilidadNoResponde_devuelve503ConElMensajeAcordado() throws Exception {
        PedidoHandler handler = mock(PedidoHandler.class);
        when(handler.realizarPedido(any(Pedido.class))).thenThrow(
                new ServicioTrazabilidadNoDisponibleException(
                        MensajeError.TRAZABILIDAD_NO_DISPONIBLE, new RuntimeException("conexion rechazada")));
        MockMvc mockMvc = MockMvcBuilders
                .standaloneSetup(new PedidoRestController(handler, Mappers.getMapper(PedidoRestMapper.class)))
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        UsuarioAutenticado cliente = new UsuarioAutenticado(7L, "cli@mail.com", RolUsuario.CLIENTE);
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                cliente, null, List.of(new SimpleGrantedAuthority("ROLE_CLIENTE"))));

        mockMvc.perform(post("/api/v1/pedidos").contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"idRestaurante\": 1, \"platos\": [ { \"idPlato\": 10, \"cantidad\": 1 } ] }"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.mensaje").value(MensajeError.TRAZABILIDAD_NO_DISPONIBLE))
                .andExpect(jsonPath("$.status").value(503));
    }
}