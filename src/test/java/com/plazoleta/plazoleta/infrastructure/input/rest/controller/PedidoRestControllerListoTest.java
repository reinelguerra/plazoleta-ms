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
import com.plazoleta.plazoleta.infrastructure.exception.ServicioMensajeriaNoDisponibleException;
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

/** HU 14 en el controller: el empleado sale del token, el pedido de la URL y el pin no se expone. */
class PedidoRestControllerListoTest {

    private static final String URL = "/api/v1/pedidos/40/listo";
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

    @Test
    void marcarListo_usaElEmpleadoDelToken_yDevuelveElPedidoListoSinPin() throws Exception {
        Pedido pedido = new Pedido();
        pedido.setId(40L);
        pedido.setIdCliente(7L);
        pedido.setIdRestaurante(3L);
        pedido.setEstado(EstadoPedido.LISTO);
        pedido.setIdEmpleado(ID_EMPLEADO);
        pedido.setPin("123456");
        pedido.setFecha(LocalDateTime.of(2026, 11, 14, 10, 0));
        pedido.setPlatos(List.of(new PedidoPlato(10L, 2)));
        when(handler.marcarPedidoListo(40L, ID_EMPLEADO)).thenReturn(pedido);

        mockMvc.perform(patch(URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(40))
                .andExpect(jsonPath("$.estado").value("LISTO"))
                .andExpect(jsonPath("$.idEmpleado").value(12))
                .andExpect(jsonPath("$.pin").doesNotExist());

        verify(handler).marcarPedidoListo(40L, ID_EMPLEADO);
    }

    @Test
    void pedidoInexistente_devuelve404() throws Exception {
        when(handler.marcarPedidoListo(40L, ID_EMPLEADO)).thenThrow(new PedidoNoEncontradoException(40L));

        mockMvc.perform(patch(URL))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void pedidoQueNoEstaEnPreparacion_devuelve400ConElMensajeDelDominio() throws Exception {
        when(handler.marcarPedidoListo(40L, ID_EMPLEADO))
                .thenThrow(new DomainException(MensajeError.PEDIDO_NO_EN_PREPARACION));

        mockMvc.perform(patch(URL))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value(MensajeError.PEDIDO_NO_EN_PREPARACION));
    }

    @Test
    void clienteSinCelular_devuelve400() throws Exception {
        when(handler.marcarPedidoListo(40L, ID_EMPLEADO))
                .thenThrow(new DomainException(MensajeError.CLIENTE_SIN_CELULAR));

        mockMvc.perform(patch(URL))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value(MensajeError.CLIENTE_SIN_CELULAR));
    }

    @Test
    void mensajeriaCaida_devuelve503() throws Exception {
        when(handler.marcarPedidoListo(40L, ID_EMPLEADO)).thenThrow(
                new ServicioMensajeriaNoDisponibleException(MensajeError.MENSAJERIA_NO_DISPONIBLE,
                        new RuntimeException("sin conexion")));

        mockMvc.perform(patch(URL))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.mensaje").value(MensajeError.MENSAJERIA_NO_DISPONIBLE))
                .andExpect(jsonPath("$.status").value(503));
    }
}