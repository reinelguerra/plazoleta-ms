package com.plazoleta.plazoleta.infrastructure.input.rest.controller;

import com.plazoleta.plazoleta.application.handler.PedidoHandler;
import com.plazoleta.plazoleta.domain.exception.DomainException;
import com.plazoleta.plazoleta.domain.exception.PlatoNoEncontradoException;
import com.plazoleta.plazoleta.domain.exception.RestauranteNoEncontradoException;
import com.plazoleta.plazoleta.domain.model.EstadoPedido;
import com.plazoleta.plazoleta.domain.model.Pedido;
import com.plazoleta.plazoleta.domain.model.RolUsuario;
import com.plazoleta.plazoleta.domain.util.MensajeError;
import com.plazoleta.plazoleta.infrastructure.exception.GlobalExceptionHandler;
import com.plazoleta.plazoleta.infrastructure.input.rest.mapper.PedidoRestMapper;
import com.plazoleta.plazoleta.infrastructure.security.UsuarioAutenticado;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PedidoRestControllerTest {

    private static final String URL = "/api/v1/pedidos";
    private static final Long ID_CLIENTE = 7L;

    private static final String JSON_VALIDO = """
            { "idRestaurante": 1,
              "platos": [ { "idPlato": 10, "cantidad": 2 }, { "idPlato": 11, "cantidad": 1 } ] }
            """;

    private PedidoHandler handler;
    private MockMvc mockMvc;

    @BeforeEach
    void preparar() {
        handler = mock(PedidoHandler.class);
        PedidoRestController controller = new PedidoRestController(
                handler, Mappers.getMapper(PedidoRestMapper.class));
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        autenticarComoCliente(ID_CLIENTE);
    }

    @AfterEach
    void limpiar() {
        SecurityContextHolder.clearContext();
    }

    /** Simula lo que hace el filtro JWT: deja al cliente autenticado. */
    private void autenticarComoCliente(Long id) {
        UsuarioAutenticado usuario = new UsuarioAutenticado(id, "cli@mail.com", RolUsuario.CLIENTE);
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                usuario, null, List.of(new SimpleGrantedAuthority("ROLE_CLIENTE"))));
    }

    private org.springframework.test.web.servlet.ResultActions enviar(String json) throws Exception {
        return mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(json));
    }

    @Test
    void pedidoValido_devuelve201_ConElClienteDelToken() throws Exception {
        when(handler.realizarPedido(any(Pedido.class))).thenAnswer(invocacion -> {
            Pedido pedido = invocacion.getArgument(0);
            pedido.setId(1L);
            pedido.setEstado(EstadoPedido.PENDIENTE);
            pedido.setFecha(LocalDateTime.of(2026, 11, 11, 15, 0));
            return pedido;
        });

        enviar(JSON_VALIDO)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.idCliente").value(7))
                .andExpect(jsonPath("$.idRestaurante").value(1))
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.platos.length()").value(2))
                .andExpect(jsonPath("$.platos[0].idPlato").value(10))
                .andExpect(jsonPath("$.platos[0].cantidad").value(2))
                .andExpect(jsonPath("$.pin").doesNotExist());

        ArgumentCaptor<Pedido> captor = ArgumentCaptor.forClass(Pedido.class);
        verify(handler).realizarPedido(captor.capture());
        assertEquals(ID_CLIENTE, captor.getValue().getIdCliente());
        assertEquals(1L, captor.getValue().getIdRestaurante());
        assertEquals(2, captor.getValue().getPlatos().size());
    }

    @Test
    void elClienteDelBodyNoSeUsa_siempreEsElDelToken() throws Exception {
        when(handler.realizarPedido(any(Pedido.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        enviar("""
                { "idCliente": 999, "idRestaurante": 1, "platos": [ { "idPlato": 10, "cantidad": 1 } ] }
                """)
                .andExpect(status().isCreated());

        ArgumentCaptor<Pedido> captor = ArgumentCaptor.forClass(Pedido.class);
        verify(handler).realizarPedido(captor.capture());
        assertEquals(ID_CLIENTE, captor.getValue().getIdCliente());
    }

    @Test
    void sinPlatos_devuelve400SinLlamarAlHandler() throws Exception {
        enviar("{ \"idRestaurante\": 1, \"platos\": [] }")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value(MensajeError.PEDIDO_SIN_PLATOS))
                .andExpect(jsonPath("$.status").value(400));

        verifyNoInteractions(handler);
    }

    @Test
    void sinCampoPlatos_devuelve400() throws Exception {
        enviar("{ \"idRestaurante\": 1 }")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value(MensajeError.PEDIDO_SIN_PLATOS));

        verifyNoInteractions(handler);
    }

    @Test
    void sinRestaurante_devuelve400() throws Exception {
        enviar("{ \"platos\": [ { \"idPlato\": 10, \"cantidad\": 1 } ] }")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value(MensajeError.RESTAURANTE_OBLIGATORIO));

        verifyNoInteractions(handler);
    }

    @Test
    void cantidadCero_devuelve400() throws Exception {
        enviar("{ \"idRestaurante\": 1, \"platos\": [ { \"idPlato\": 10, \"cantidad\": 0 } ] }")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value(MensajeError.CANTIDAD_INVALIDA));

        verifyNoInteractions(handler);
    }

    @Test
    void cantidadNegativa_devuelve400() throws Exception {
        enviar("{ \"idRestaurante\": 1, \"platos\": [ { \"idPlato\": 10, \"cantidad\": -3 } ] }")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value(MensajeError.CANTIDAD_INVALIDA));

        verifyNoInteractions(handler);
    }

    @Test
    void platoSinCantidad_devuelve400() throws Exception {
        enviar("{ \"idRestaurante\": 1, \"platos\": [ { \"idPlato\": 10 } ] }")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value(MensajeError.CANTIDAD_INVALIDA));

        verifyNoInteractions(handler);
    }

    @Test
    void platoSinId_devuelve400() throws Exception {
        enviar("{ \"idRestaurante\": 1, \"platos\": [ { \"cantidad\": 1 } ] }")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value(MensajeError.PLATO_PEDIDO_INVALIDO));

        verifyNoInteractions(handler);
    }

    @Test
    void jsonMalFormado_devuelve400() throws Exception {
        enviar("{ \"idRestaurante\": ")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verifyNoInteractions(handler);
    }

    @Test
    void clienteConPedidoEnProceso_devuelve400ConElMensajeDelDominio() throws Exception {
        when(handler.realizarPedido(any(Pedido.class)))
                .thenThrow(new DomainException(MensajeError.CLIENTE_CON_PEDIDO_EN_PROCESO));

        enviar(JSON_VALIDO)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value(MensajeError.CLIENTE_CON_PEDIDO_EN_PROCESO));
    }

    @Test
    void restauranteInexistente_devuelve404() throws Exception {
        when(handler.realizarPedido(any(Pedido.class))).thenThrow(new RestauranteNoEncontradoException(1L));

        enviar(JSON_VALIDO)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void platoInexistente_devuelve404() throws Exception {
        when(handler.realizarPedido(any(Pedido.class))).thenThrow(new PlatoNoEncontradoException(10L));

        enviar(JSON_VALIDO)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}