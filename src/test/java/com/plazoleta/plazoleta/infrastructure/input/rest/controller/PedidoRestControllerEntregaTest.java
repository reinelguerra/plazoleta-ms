package com.plazoleta.plazoleta.infrastructure.input.rest.controller;

import com.plazoleta.plazoleta.application.handler.PedidoHandler;
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
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** HU 15 en el controller: el empleado sale del token, el PIN del cuerpo y el pin no se expone. */
class PedidoRestControllerEntregaTest {

    private static final String URL = "/api/v1/pedidos/40/entrega";
    private static final Long ID_EMPLEADO = 12L;

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
        UsuarioAutenticado empleado = new UsuarioAutenticado(ID_EMPLEADO, "luis@mail.com", RolUsuario.EMPLEADO);
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                empleado, null, List.of(new SimpleGrantedAuthority("ROLE_EMPLEADO"))));
    }

    @AfterEach
    void limpiar() {
        SecurityContextHolder.clearContext();
    }

    private ResultActions entregar(String cuerpo) throws Exception {
        return mockMvc.perform(patch(URL).contentType(MediaType.APPLICATION_JSON).content(cuerpo));
    }

    @Test
    void entregar_usaElEmpleadoDelTokenYElPinDelCuerpo_yDevuelveElPedidoEntregadoSinPin() throws Exception {
        Pedido pedido = new Pedido();
        pedido.setId(40L);
        pedido.setIdCliente(7L);
        pedido.setIdRestaurante(3L);
        pedido.setEstado(EstadoPedido.ENTREGADO);
        pedido.setIdEmpleado(ID_EMPLEADO);
        pedido.setPin("123456");
        pedido.setFecha(LocalDateTime.of(2026, 11, 15, 10, 0));
        pedido.setPlatos(List.of(new PedidoPlato(10L, 2)));
        when(handler.entregarPedido(40L, ID_EMPLEADO, "123456")).thenReturn(pedido);

        entregar("{\"pin\":\"123456\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(40))
                .andExpect(jsonPath("$.estado").value("ENTREGADO"))
                .andExpect(jsonPath("$.pin").doesNotExist());

        verify(handler).entregarPedido(40L, ID_EMPLEADO, "123456");
    }

    @Test
    void pinIncorrecto_devuelve400ConElMensajeDelDominio() throws Exception {
        when(handler.entregarPedido(40L, ID_EMPLEADO, "000000"))
                .thenThrow(new DomainException(MensajeError.PIN_INCORRECTO));

        entregar("{\"pin\":\"000000\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value(MensajeError.PIN_INCORRECTO));
    }

    @Test
    void pedidoQueNoEstaListo_devuelve400() throws Exception {
        when(handler.entregarPedido(40L, ID_EMPLEADO, "123456"))
                .thenThrow(new DomainException(MensajeError.PEDIDO_NO_LISTO));

        entregar("{\"pin\":\"123456\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value(MensajeError.PEDIDO_NO_LISTO));
    }

    @Test
    void pedidoInexistente_devuelve404() throws Exception {
        when(handler.entregarPedido(40L, ID_EMPLEADO, "123456")).thenThrow(new PedidoNoEncontradoException(40L));

        entregar("{\"pin\":\"123456\"}")
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void sinPin_devuelve400YNoLlamaAlHandler() throws Exception {
        entregar("{}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value(MensajeError.PIN_OBLIGATORIO));

        verify(handler, never()).entregarPedido(any(), any(), any());
    }

    @Test
    void pinConLetras_devuelve400YNoLlamaAlHandler() throws Exception {
        entregar("{\"pin\":\"abc123\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value(MensajeError.PIN_OBLIGATORIO));

        verify(handler, never()).entregarPedido(any(), any(), any());
    }
}