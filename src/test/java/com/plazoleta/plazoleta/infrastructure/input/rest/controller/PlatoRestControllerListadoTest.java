package com.plazoleta.plazoleta.infrastructure.input.rest.controller;

import com.plazoleta.plazoleta.application.handler.PlatoHandler;
import com.plazoleta.plazoleta.domain.exception.DomainException;
import com.plazoleta.plazoleta.domain.exception.RestauranteNoEncontradoException;
import com.plazoleta.plazoleta.domain.model.PaginaRespuesta;
import com.plazoleta.plazoleta.domain.model.Plato;
import com.plazoleta.plazoleta.infrastructure.exception.GlobalExceptionHandler;
import com.plazoleta.plazoleta.infrastructure.input.rest.mapper.PlatoRestMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PlatoRestControllerListadoTest {

    private static final String URL = "/api/v1/platos/restaurante/1";

    private PlatoHandler handler;
    private MockMvc mockMvc;

    @BeforeEach
    void preparar() {
        handler = mock(PlatoHandler.class);
        PlatoRestController controller = new PlatoRestController(
                handler, Mappers.getMapper(PlatoRestMapper.class));
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private Plato plato(String nombre) {
        return new Plato(5L, nombre, 25000, "Descripcion", "https://img.com/x.png", "Postre", true, 1L);
    }

    @Test
    void listar_sinParametros_usaPagina0Tamanio10_yNoExponeActivoNiRestaurante() throws Exception {
        PaginaRespuesta<Plato> pagina = new PaginaRespuesta<>(List.of(plato("Flan"), plato("Helado")), 0, 10, 2, 1);
        when(handler.listarPlatos(1L, null, 0, 10)).thenReturn(pagina);

        mockMvc.perform(get(URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenido.length()").value(2))
                .andExpect(jsonPath("$.contenido[0].id").value(5))
                .andExpect(jsonPath("$.contenido[0].nombre").value("Flan"))
                .andExpect(jsonPath("$.contenido[0].precio").value(25000))
                .andExpect(jsonPath("$.contenido[0].categoria").value("Postre"))
                .andExpect(jsonPath("$.contenido[0].activo").doesNotExist())
                .andExpect(jsonPath("$.contenido[0].idRestaurante").doesNotExist())
                .andExpect(jsonPath("$.pagina").value(0))
                .andExpect(jsonPath("$.tamanio").value(10))
                .andExpect(jsonPath("$.totalElementos").value(2))
                .andExpect(jsonPath("$.totalPaginas").value(1));
    }

    @Test
    void listar_conCategoriaYPaginacion_losPasaAlHandler() throws Exception {
        when(handler.listarPlatos(1L, "Postre", 1, 5)).thenReturn(new PaginaRespuesta<>(List.of(), 1, 5, 0, 0));

        mockMvc.perform(get(URL).param("categoria", "Postre").param("pagina", "1").param("tamanio", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenido.length()").value(0));

        verify(handler).listarPlatos(1L, "Postre", 1, 5);
    }

    @Test
    void listar_restauranteInexistente_devuelve404() throws Exception {
        when(handler.listarPlatos(1L, null, 0, 10)).thenThrow(new RestauranteNoEncontradoException(1L));

        mockMvc.perform(get(URL))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void listar_paginacionInvalida_devuelve400() throws Exception {
        when(handler.listarPlatos(1L, null, -1, 10))
                .thenThrow(new DomainException("La página no puede ser negativa"));

        mockMvc.perform(get(URL).param("pagina", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("La página no puede ser negativa"))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void listar_idDeRestauranteNoNumerico_devuelve400() throws Exception {
        mockMvc.perform(get("/api/v1/platos/restaurante/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }
}