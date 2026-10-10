package com.plazoleta.plazoleta.infrastructure.input.rest;

import com.plazoleta.plazoleta.domain.api.PlatoServicePort;
import com.plazoleta.plazoleta.domain.exception.PlatoNoEncontradoException;
import com.plazoleta.plazoleta.domain.exception.PropietarioNoAutorizadoException;
import com.plazoleta.plazoleta.domain.model.Plato;
import com.plazoleta.plazoleta.infrastructure.exception.GlobalExceptionHandler;
import com.plazoleta.plazoleta.infrastructure.input.rest.mapper.PlatoRestMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PlatoRestControllerModificarTest {

    private static final String URL = "/api/v1/platos/5";
    private static final String CABECERA = "X-Propietario-Id";
    private static final String JSON_VALIDO = """
            { "precio": 30000, "descripcion": "Nueva descripcion" }
            """;

    private PlatoServicePort servicePort;
    private MockMvc mockMvc;

    @BeforeEach
    void preparar() {
        servicePort = mock(PlatoServicePort.class);
        PlatoRestController controller = new PlatoRestController(
                servicePort, Mappers.getMapper(PlatoRestMapper.class));
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void modificar_valido_devuelve200ConElPlatoActualizado() throws Exception {
        Plato modificado = new Plato(5L, "Bandeja", 30000, "Nueva descripcion",
                "https://img.com/b.png", "Fuerte", true, 10L);
        when(servicePort.modificarPlato(5L, 30000, "Nueva descripcion", 7L)).thenReturn(modificado);

        mockMvc.perform(patch(URL).header(CABECERA, 7)
                        .contentType(MediaType.APPLICATION_JSON).content(JSON_VALIDO))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.nombre").value("Bandeja"))
                .andExpect(jsonPath("$.precio").value(30000))
                .andExpect(jsonPath("$.descripcion").value("Nueva descripcion"));

        verify(servicePort).modificarPlato(5L, 30000, "Nueva descripcion", 7L);
    }

    @Test
    void modificar_precioConDecimales_devuelve400() throws Exception {
        mockMvc.perform(patch(URL).header(CABECERA, 7)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"precio\": 100.5, \"descripcion\": \"x\" }"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verifyNoInteractions(servicePort);
    }

    @Test
    void modificar_platoInexistente_devuelve404() throws Exception {
        when(servicePort.modificarPlato(5L, 30000, "Nueva descripcion", 7L))
                .thenThrow(new PlatoNoEncontradoException(5L));

        mockMvc.perform(patch(URL).header(CABECERA, 7)
                        .contentType(MediaType.APPLICATION_JSON).content(JSON_VALIDO))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void modificar_platoDeOtroPropietario_devuelve403() throws Exception {
        when(servicePort.modificarPlato(5L, 30000, "Nueva descripcion", 8L))
                .thenThrow(new PropietarioNoAutorizadoException());

        mockMvc.perform(patch(URL).header(CABECERA, 8)
                        .contentType(MediaType.APPLICATION_JSON).content(JSON_VALIDO))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void modificar_sinCabeceraDelPropietario_devuelve400() throws Exception {
        mockMvc.perform(patch(URL)
                        .contentType(MediaType.APPLICATION_JSON).content(JSON_VALIDO))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verifyNoInteractions(servicePort);
    }
}