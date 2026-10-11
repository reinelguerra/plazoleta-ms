package com.plazoleta.plazoleta.infrastructure.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.headerDoesNotExist;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class UsuariosClientConfigurationTest {

    private static final String BASE_URL = "http://localhost:8081/api/v1";

    @AfterEach
    void limpiar() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void reenviaLaCabeceraAuthorizationDeLaPeticionEnCurso() {
        MockHttpServletRequest entrante = new MockHttpServletRequest();
        entrante.addHeader("Authorization", "Bearer token-del-usuario");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(entrante));

        RestClient.Builder builder = UsuariosClientConfiguration.crearBuilder(BASE_URL);
        MockRestServiceServer servidor = MockRestServiceServer.bindTo(builder).build();
        servidor.expect(requestTo(BASE_URL + "/usuarios/5"))
                .andExpect(header("Authorization", "Bearer token-del-usuario"))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        builder.build().get().uri("/usuarios/5").retrieve().toBodilessEntity();

        servidor.verify();
    }

    @Test
    void sinPeticionEnCurso_noAgregaLaCabecera() {
        RestClient.Builder builder = UsuariosClientConfiguration.crearBuilder(BASE_URL);
        MockRestServiceServer servidor = MockRestServiceServer.bindTo(builder).build();
        servidor.expect(requestTo(BASE_URL + "/usuarios/5"))
                .andExpect(headerDoesNotExist("Authorization"))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        builder.build().get().uri("/usuarios/5").retrieve().toBodilessEntity();

        servidor.verify();
    }

    @Test
    void peticionEnCursoSinToken_noAgregaLaCabecera() {
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest()));

        RestClient.Builder builder = UsuariosClientConfiguration.crearBuilder(BASE_URL);
        MockRestServiceServer servidor = MockRestServiceServer.bindTo(builder).build();
        servidor.expect(requestTo(BASE_URL + "/usuarios/5"))
                .andExpect(headerDoesNotExist("Authorization"))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        builder.build().get().uri("/usuarios/5").retrieve().toBodilessEntity();

        servidor.verify();
    }
}