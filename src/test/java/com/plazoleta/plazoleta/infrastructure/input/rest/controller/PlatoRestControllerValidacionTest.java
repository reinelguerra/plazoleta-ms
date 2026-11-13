package com.plazoleta.plazoleta.infrastructure.input.rest.controller;

import com.plazoleta.plazoleta.application.handler.PlatoHandler;
import com.plazoleta.plazoleta.domain.model.Plato;
import com.plazoleta.plazoleta.domain.model.RolUsuario;
import com.plazoleta.plazoleta.domain.util.MensajeError;
import com.plazoleta.plazoleta.infrastructure.exception.GlobalExceptionHandler;
import com.plazoleta.plazoleta.infrastructure.input.rest.mapper.PlatoRestMapper;
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
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** @Valid en los endpoints de platos: un cuerpo inválido da 400 y nunca llega al handler. */
class PlatoRestControllerValidacionTest {

    private PlatoHandler handler;
    private MockMvc mockMvc;

    @BeforeEach
    void preparar() {
        handler = mock(PlatoHandler.class);
        mockMvc = MockMvcBuilders
                .standaloneSetup(new PlatoRestController(handler, Mappers.getMapper(PlatoRestMapper.class)))
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        UsuarioAutenticado propietario = new UsuarioAutenticado(5L, "p@mail.com", RolUsuario.PROPIETARIO);
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                propietario, null, List.of(new SimpleGrantedAuthority("ROLE_PROPIETARIO"))));
    }

    @AfterEach
    void limpiar() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void crear_cuerpoInvalido_devuelve400ConTodosLosMensajes() throws Exception {
        String json = "{\"nombre\":\" \",\"precio\":0,\"descripcion\":\"\",\"urlImagen\":\"\","
                + "\"categoria\":\"\",\"idRestaurante\":null}";

        mockMvc.perform(post("/api/v1/platos").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje", containsString(MensajeError.NOMBRE_PLATO_OBLIGATORIO)))
                .andExpect(jsonPath("$.mensaje", containsString(MensajeError.PRECIO_PLATO_INVALIDO)))
                .andExpect(jsonPath("$.mensaje", containsString(MensajeError.RESTAURANTE_PLATO_OBLIGATORIO)));

        verifyNoInteractions(handler);
    }

    @Test
    void crear_cuerpoValido_llegaAlHandlerConElPropietarioDelToken() throws Exception {
        Plato creado = new Plato(1L, "Bandeja", 25000, "d", "u", "c", true, 1L);
        when(handler.crearPlato(any(Plato.class), anyLong())).thenReturn(creado);
        String json = "{\"nombre\":\"Bandeja\",\"precio\":25000,\"descripcion\":\"d\",\"urlImagen\":\"u\","
                + "\"categoria\":\"c\",\"idRestaurante\":1}";

        mockMvc.perform(post("/api/v1/platos").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.precio").value(25000));

        verify(handler).crearPlato(any(Plato.class), org.mockito.ArgumentMatchers.eq(5L));
    }

    @Test
    void modificar_precioNegativo_devuelve400() throws Exception {
        mockMvc.perform(patch("/api/v1/platos/1").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"precio\":-5,\"descripcion\":\"algo\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value(MensajeError.PRECIO_PLATO_INVALIDO));

        verifyNoInteractions(handler);
    }

    @Test
    void cambiarEstado_sinActivo_devuelve400() throws Exception {
        mockMvc.perform(patch("/api/v1/platos/1/estado").contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value(MensajeError.ESTADO_PLATO_OBLIGATORIO));

        verifyNoInteractions(handler);
    }
}