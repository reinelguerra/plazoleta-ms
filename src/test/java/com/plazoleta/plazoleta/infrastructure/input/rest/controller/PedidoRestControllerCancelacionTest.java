package com.plazoleta.plazoleta.infrastructure.input.rest.controller;

import com.plazoleta.plazoleta.application.handler.PedidoHandler;
import com.plazoleta.plazoleta.domain.exception.ClienteNoAutorizadoException;
import com.plazoleta.plazoleta.domain.exception.DomainException;
import com.plazoleta.plazoleta.domain.exception.PedidoNoEncontradoException;
import com.plazoleta.plazoleta.domain.model.EstadoPedido;
import com.plazoleta.plazoleta.domain.model.Pedido;
import com.plazoleta.plazoleta.domain.model.PedidoPlato;
import com.plazoleta.plazoleta.domain.model.RolUsuario;
import com.plazoleta.plazoleta.domain.util.MensajeError;
import com.plazoleta.plazoleta.infrastructure.exception.GlobalExceptionHandler;
import com.plazoleta.plazoleta.infrastructure.input.rest.mapper.PedidoRestMapper;
import com.plazoleta.plazoleta.infrastructure.security.UsuarioAutenticado;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** HU 16 en el controller: el cliente sale del token y cada error tiene su código HTTP. */
class PedidoRestControllerCancelacionTest {

    private static final String URL = "/api/v1/pedidos/40/cancelacion";
    private static final Long ID_CLIENTE = 7L;

    private PedidoHandler handler;
    private MockMvc mockMvc;

    @BeforeEach
    void preparar() {
        handler = mock(PedidoHandler.class);
        mockMvc = MockMvcBuilders
                .standaloneSetup(new PedidoRestController(handler, Mappers.getMapper(PedidoRestMapper.class)))
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        UsuarioAutenticado cliente = new UsuarioAutenticado(ID_CLIENTE, "ana@mail.com", RolUsuario.CLIENTE);
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                cliente, null, List.of(new SimpleGrantedAuthority("ROLE_CLIENTE"))));
    }

    @AfterEach
    void limpiar() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void cancelar_usaElClienteDelToken_yDevuelveElPedidoCancelado() throws Exception {
        Pedido pedido = new Pedido();
        pedido.setId(40L);
        pedido.setIdCliente(ID_CLIENTE);
        pedido.setIdRestaurante(3L);
        pedido.setEstado(EstadoPedido.CANCELADO);
        pedido.setFecha(LocalDateTime.of(2026, 11, 12, 10, 0));
        pedido.setPlatos(List.of(new PedidoPlato(10L, 2)));
        when(handler.cancelarPedido(40L, ID_CLIENTE)).thenReturn(pedido);

        mockMvc.perform(patch(URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(40))
                .andExpect(jsonPath("$.estado").value("CANCELADO"))
                .andExpect(jsonPath("$.pin").doesNotExist());

        verify(handler).cancelarPedido(40L, ID_CLIENTE);
    }

    @Test
    void pedidoInexistente_devuelve404() throws Exception {
        when(handler.cancelarPedido(40L, ID_CLIENTE)).thenThrow(new PedidoNoEncontradoException(40L));

        mockMvc.perform(patch(URL))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void pedidoDeOtroCliente_devuelve403() throws Exception {
        when(handler.cancelarPedido(40L, ID_CLIENTE)).thenThrow(new ClienteNoAutorizadoException());

        mockMvc.perform(patch(URL))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.mensaje").value(MensajeError.PEDIDO_DE_OTRO_CLIENTE));
    }

    @Test
    void pedidoYaEnPreparacion_devuelve400ConElMensajeDelEnunciado() throws Exception {
        when(handler.cancelarPedido(40L, ID_CLIENTE))
                .thenThrow(new DomainException(MensajeError.PEDIDO_NO_CANCELABLE));

        mockMvc.perform(patch(URL))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value(MensajeError.PEDIDO_NO_CANCELABLE));
    }
}