package com.plazoleta.plazoleta.infrastructure.input.rest.controller;

import com.plazoleta.plazoleta.domain.api.RestauranteServicePort;
import com.plazoleta.plazoleta.domain.exception.DomainException;
import com.plazoleta.plazoleta.domain.exception.UsuarioNoEncontradoException;
import com.plazoleta.plazoleta.domain.model.Restaurante;
import com.plazoleta.plazoleta.infrastructure.exception.GlobalExceptionHandler;
import com.plazoleta.plazoleta.infrastructure.exception.ServicioUsuariosNoDisponibleException;
import com.plazoleta.plazoleta.infrastructure.input.rest.mapper.RestauranteRequestMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Prueba del controlador y del manejador de errores sin levantar Spring ni la base de datos.
 * El puerto del dominio es un doble de Mockito.
 */
class RestauranteControllerTest {

    private static final String URL = "/api/v1/restaurantes";

    private static final String JSON_VALIDO = """
            {
              "nombre": "Sabor 24",
              "nit": "900123456",
              "direccion": "Calle 10 # 5-20",
              "telefono": "+573001234567",
              "urlLogo": "https://logo.com/sabor.png",
              "idPropietario": 1
            }
            """;

    private RestauranteServicePort servicePort;
    private MockMvc mockMvc;

    @BeforeEach
    void preparar() {
        servicePort = mock(RestauranteServicePort.class);
        RestauranteController controller = new RestauranteController(
                servicePort, Mappers.getMapper(RestauranteRequestMapper.class));
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void crearRestaurante_valido_devuelve201ConElRestauranteCreado() throws Exception {
        Restaurante creado = new Restaurante(10L, "Sabor 24", "900123456", "Calle 10 # 5-20",
                "+573001234567", "https://logo.com/sabor.png", 1L);
        when(servicePort.crearRestaurante(any(Restaurante.class))).thenReturn(creado);

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(JSON_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.nombre").value("Sabor 24"))
                .andExpect(jsonPath("$.idPropietario").value(1));

        ArgumentCaptor<Restaurante> captor = ArgumentCaptor.forClass(Restaurante.class);
        verify(servicePort).crearRestaurante(captor.capture());
        assertEquals(1L, captor.getValue().getIdPropietario());
        assertEquals("900123456", captor.getValue().getNit());
    }

    @Test
    void reglaDeNegocioRota_devuelve400ConElFormatoAcordado() throws Exception {
        when(servicePort.crearRestaurante(any(Restaurante.class)))
                .thenThrow(new DomainException("Dato invalido"));

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(JSON_VALIDO))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Dato invalido"))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void propietarioInexistente_devuelve404() throws Exception {
        when(servicePort.crearRestaurante(any(Restaurante.class)))
                .thenThrow(new UsuarioNoEncontradoException(99L));

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(JSON_VALIDO))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void usuariosCaido_devuelve503() throws Exception {
        when(servicePort.crearRestaurante(any(Restaurante.class)))
                .thenThrow(new ServicioUsuariosNoDisponibleException("Usuarios no responde", new RuntimeException()));

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(JSON_VALIDO))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value(503));
    }

    @Test
    void jsonMalFormado_devuelve400() throws Exception {
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content("{ esto no es json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }
}