package com.plazoleta.plazoleta.infrastructure.input.rest.controller;

import com.plazoleta.plazoleta.application.handler.PedidoHandler;
import com.plazoleta.plazoleta.domain.exception.DomainException;
import com.plazoleta.plazoleta.domain.exception.UsuarioNoEncontradoException;
import com.plazoleta.plazoleta.domain.model.EstadoPedido;
import com.plazoleta.plazoleta.domain.model.PaginaRespuesta;
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
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** HU 12 en el controller: el empleado sale del token y el pin nunca se expone. */
class PedidoRestControllerListadoTest {

    private static final String URL = "/api/v1/pedidos";
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

    private Pedido pedido(Long id) {
        Pedido pedido = new Pedido();
        pedido.setId(id);
        pedido.setIdCliente(7L);
        pedido.setIdRestaurante(3L);
        pedido.setEstado(EstadoPedido.PENDIENTE);
        pedido.setFecha(LocalDateTime.of(2026, 11, 12, 10, 0));
        pedido.setPin("123456");
        pedido.setPlatos(List.of(new PedidoPlato(10L, 2)));
        return pedido;
    }

    @Test
    void listar_usaElEmpleadoDelToken_devuelveTodosLosCamposMenosElPin() throws Exception {
        when(handler.listarPedidosPorEstado(ID_EMPLEADO, EstadoPedido.PENDIENTE, 0, 10))
                .thenReturn(new PaginaRespuesta<>(List.of(pedido(1L), pedido(2L)), 0, 10, 2, 1));

        mockMvc.perform(get(URL).param("estado", "PENDIENTE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenido.length()").value(2))
                .andExpect(jsonPath("$.contenido[0].id").value(1))
                .andExpect(jsonPath("$.contenido[0].idCliente").value(7))
                .andExpect(jsonPath("$.contenido[0].idRestaurante").value(3))
                .andExpect(jsonPath("$.contenido[0].estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.contenido[0].platos[0].idPlato").value(10))
                .andExpect(jsonPath("$.contenido[0].platos[0].cantidad").value(2))
                .andExpect(jsonPath("$.contenido[0].pin").doesNotExist())
                .andExpect(jsonPath("$.pagina").value(0))
                .andExpect(jsonPath("$.tamanio").value(10))
                .andExpect(jsonPath("$.totalElementos").value(2))
                .andExpect(jsonPath("$.totalPaginas").value(1));
    }

    @Test
    void listar_conPaginacion_laPasaAlHandler() throws Exception {
        when(handler.listarPedidosPorEstado(ID_EMPLEADO, EstadoPedido.LISTO, 2, 5))
                .thenReturn(new PaginaRespuesta<>(List.of(), 2, 5, 0, 0));

        mockMvc.perform(get(URL).param("estado", "LISTO").param("pagina", "2").param("tamanio", "5"))
                .andExpect(status().isOk());

        verify(handler).listarPedidosPorEstado(ID_EMPLEADO, EstadoPedido.LISTO, 2, 5);
    }

    @Test
    void sinEstado_devuelve400() throws Exception {
        mockMvc.perform(get(URL))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verifyNoInteractions(handler);
    }

    @Test
    void estadoQueNoExiste_devuelve400() throws Exception {
        mockMvc.perform(get(URL).param("estado", "COCINANDO"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verifyNoInteractions(handler);
    }

    @Test
    void paginacionInvalida_devuelve400ConElMensajeDelDominio() throws Exception {
        when(handler.listarPedidosPorEstado(ID_EMPLEADO, EstadoPedido.PENDIENTE, -1, 10))
                .thenThrow(new DomainException(MensajeError.PAGINA_NEGATIVA));

        mockMvc.perform(get(URL).param("estado", "PENDIENTE").param("pagina", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value(MensajeError.PAGINA_NEGATIVA));
    }

    @Test
    void empleadoSinRestaurante_devuelve400() throws Exception {
        when(handler.listarPedidosPorEstado(ID_EMPLEADO, EstadoPedido.PENDIENTE, 0, 10))
                .thenThrow(new DomainException(MensajeError.EMPLEADO_SIN_RESTAURANTE));

        mockMvc.perform(get(URL).param("estado", "PENDIENTE"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value(MensajeError.EMPLEADO_SIN_RESTAURANTE));
    }

    @Test
    void empleadoInexistenteEnUsuarios_devuelve404() throws Exception {
        when(handler.listarPedidosPorEstado(ID_EMPLEADO, EstadoPedido.PENDIENTE, 0, 10))
                .thenThrow(new UsuarioNoEncontradoException(ID_EMPLEADO));

        mockMvc.perform(get(URL).param("estado", "PENDIENTE"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}