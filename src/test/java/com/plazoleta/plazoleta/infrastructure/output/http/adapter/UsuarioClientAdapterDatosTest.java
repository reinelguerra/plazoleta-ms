package com.plazoleta.plazoleta.infrastructure.output.http.adapter;

import com.plazoleta.plazoleta.domain.model.DatosUsuario;
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withResourceNotFound;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/** GET /usuarios/{id} ampliado por Reinel: ahora trae celular e idRestaurante. */
class UsuarioClientAdapterDatosTest {

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
    void empleado_devuelveRolCelularEIdRestaurante() {
        String json = "{\"id\":12,\"nombre\":\"Luis\",\"correo\":\"luis@mail.com\",\"rol\":\"EMPLEADO\","
                + "\"celular\":\"+573001112233\",\"idRestaurante\":3}";
        servidor.expect(requestTo(BASE_URL + "/usuarios/12"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        Optional<DatosUsuario> datos = adapter.obtenerUsuarioPorId(12L);

        assertTrue(datos.isPresent());
        assertEquals(12L, datos.get().id());
        assertEquals(RolUsuario.EMPLEADO, datos.get().rol());
        assertEquals("+573001112233", datos.get().celular());
        assertEquals(3L, datos.get().idRestaurante());
        servidor.verify();
    }

    @Test
    void cliente_noTieneIdRestaurante() {
        servidor.expect(requestTo(BASE_URL + "/usuarios/5"))
                .andRespond(withSuccess("{\"id\":5,\"rol\":\"CLIENTE\",\"celular\":\"+573009998877\"}",
                        MediaType.APPLICATION_JSON));

        Optional<DatosUsuario> datos = adapter.obtenerUsuarioPorId(5L);

        assertTrue(datos.isPresent());
        assertNull(datos.get().idRestaurante());
        assertEquals("+573009998877", datos.get().celular());
    }

    @Test
    void usuarioInexistente_404_devuelveVacio() {
        servidor.expect(requestTo(BASE_URL + "/usuarios/99")).andRespond(withResourceNotFound());

        assertTrue(adapter.obtenerUsuarioPorId(99L).isEmpty());
    }

    @Test
    void servidorConError500_lanzaServicioNoDisponible() {
        servidor.expect(requestTo(BASE_URL + "/usuarios/12")).andRespond(withServerError());

        assertThrows(ServicioUsuariosNoDisponibleException.class, () -> adapter.obtenerUsuarioPorId(12L));
    }
}