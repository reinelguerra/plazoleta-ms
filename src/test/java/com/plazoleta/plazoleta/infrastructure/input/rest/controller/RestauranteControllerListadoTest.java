package com.plazoleta.plazoleta.infrastructure.input.rest.controller;

import com.plazoleta.plazoleta.domain.api.RestauranteServicePort;
import com.plazoleta.plazoleta.domain.exception.DomainException;
import com.plazoleta.plazoleta.domain.model.Pagina;
import com.plazoleta.plazoleta.domain.model.Restaurante;
import com.plazoleta.plazoleta.infrastructure.exception.GlobalExceptionHandler;
import com.plazoleta.plazoleta.infrastructure.input.rest.mapper.RestauranteRequestMapper;
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

class RestauranteControllerListadoTest {

    private static final String URL = "/api/v1/restaurantes";

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

    private Restaurante restaurante(String nombre) {
        return new Restaurante(1L, nombre, "900123456", "Calle 10 # 5-20",
                "+573001234567", "https://logo.com/" + nombre.toLowerCase() + ".png", 7L);
    }

    @Test
    void listar_sinParametros_usaPagina0Tamanio10_yDevuelveSoloNombreYLogo() throws Exception {
        Pagina<Restaurante> pagina = new Pagina<>(List.of(restaurante("Alfa"), restaurante("Beta")), 0, 10, 2, 1);
        when(servicePort.listarRestaurantes(0, 10)).thenReturn(pagina);

        mockMvc.perform(get(URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenido.length()").value(2))
                .andExpect(jsonPath("$.contenido[0].nombre").value("Alfa"))
                .andExpect(jsonPath("$.contenido[0].urlLogo").value("https://logo.com/alfa.png"))
                .andExpect(jsonPath("$.contenido[0].nit").doesNotExist())
                .andExpect(jsonPath("$.contenido[0].idPropietario").doesNotExist())
                .andExpect(jsonPath("$.pagina").value(0))
                .andExpect(jsonPath("$.tamanio").value(10))
                .andExpect(jsonPath("$.totalElementos").value(2))
                .andExpect(jsonPath("$.totalPaginas").value(1));
    }

    @Test
    void listar_conParametros_losPasaAlCasoDeUso() throws Exception {
        when(servicePort.listarRestaurantes(2, 5)).thenReturn(new Pagina<>(List.of(), 2, 5, 0, 0));

        mockMvc.perform(get(URL).param("pagina", "2").param("tamanio", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenido.length()").value(0));

        verify(servicePort).listarRestaurantes(2, 5);
    }

    @Test
    void listar_conParametrosInvalidosParaElDominio_devuelve400() throws Exception {
        when(servicePort.listarRestaurantes(-1, 10))
                .thenThrow(new DomainException("La página no puede ser negativa"));

        mockMvc.perform(get(URL).param("pagina", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("La página no puede ser negativa"))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void listar_conParametroNoNumerico_devuelve400ConElFormatoAcordado() throws Exception {
        mockMvc.perform(get(URL).param("pagina", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }
}