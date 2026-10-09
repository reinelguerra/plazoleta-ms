package com.plazoleta.plazoleta.infrastructure.output.http.adapter;

import com.plazoleta.plazoleta.domain.model.RolUsuario;
import com.plazoleta.plazoleta.infrastructure.exception.ServicioUsuariosNoDisponibleException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withResourceNotFound;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * Prueba unitaria sin levantar Spring ni usuarios-ms: un servidor simulado
 * responde lo que diría el contrato acordado con Reinel.
 */
class UsuarioClientAdapterTest {

    private static final String BASE_URL = "http://localhost:8081/api/v1";

    private MockRestServiceServer servidor;
    private UsuarioClientAdapter adapter;

    @BeforeEach
    void preparar() {
        RestClient.Builder builder = RestClient.builder().baseUrl(BASE_URL);
        servidor = MockRestServiceServer.bindTo(builder).build();
        adapter = new UsuarioClientAdapter(builder.build());
    }

    @Test
    void usuarioExistente_devuelveSuRol_ignorandoCamposExtra() {
        String json = "{\"id\":5,\"nombre\":\"Ana\",\"apellido\":\"Diaz\","
                + "\"correo\":\"ana@mail.com\",\"rol\":\"PROPIETARIO\"}";
        servidor.expect(requestTo(BASE_URL + "/usuarios/5"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        Optional<RolUsuario> rol = adapter.obtenerRolPorId(5L);

        assertEquals(Optional.of(RolUsuario.PROPIETARIO), rol);
        servidor.verify();
    }

    @Test
    void usuarioInexistente_404_devuelveVacio() {
        servidor.expect(requestTo(BASE_URL + "/usuarios/99"))
                .andRespond(withResourceNotFound());

        Optional<RolUsuario> rol = adapter.obtenerRolPorId(99L);

        assertTrue(rol.isEmpty());
        servidor.verify();
    }

    @Test
    void servidorConError500_lanzaServicioNoDisponible() {
        servidor.expect(requestTo(BASE_URL + "/usuarios/5"))
                .andRespond(withServerError());

        assertThrows(ServicioUsuariosNoDisponibleException.class,
                () -> adapter.obtenerRolPorId(5L));
    }

    @Test
    void rolDesconocido_lanzaServicioNoDisponible() {
        servidor.expect(requestTo(BASE_URL + "/usuarios/5"))
                .andRespond(withSuccess("{\"id\":5,\"rol\":\"SUPERADMIN\"}", MediaType.APPLICATION_JSON));

        assertThrows(ServicioUsuariosNoDisponibleException.class,
                () -> adapter.obtenerRolPorId(5L));
    }
}