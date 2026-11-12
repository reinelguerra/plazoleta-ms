package com.plazoleta.plazoleta.infrastructure.output.http.adapter;

import com.plazoleta.plazoleta.domain.model.EstadoPedido;
import com.plazoleta.plazoleta.domain.model.RegistroTrazabilidad;
import com.plazoleta.plazoleta.infrastructure.exception.ServicioTrazabilidadNoDisponibleException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;

/** Sin levantar Spring ni trazabilidad-ms: un servidor simulado responde lo que dice el contrato. */
class TrazabilidadClientAdapterTest {

    private static final String BASE_URL = "http://localhost:8084/api/v1";

    private MockRestServiceServer servidor;
    private TrazabilidadClientAdapter adapter;

    @BeforeEach
    void preparar() {
        RestClient.Builder builder = RestClient.builder().baseUrl(BASE_URL);
        servidor = MockRestServiceServer.bindTo(builder).build();
        adapter = new TrazabilidadClientAdapter(builder.build());
    }

    @Test
    void logInicial_enviaElJsonDelContrato_sinEstadoAnteriorNiEmpleado() {
        servidor.expect(requestTo(BASE_URL + "/trazabilidad"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.idPedido").value(50))
                .andExpect(jsonPath("$.idCliente").value(7))
                .andExpect(jsonPath("$.idRestaurante").value(1))
                .andExpect(jsonPath("$.estadoNuevo").value("PENDIENTE"))
                .andExpect(jsonPath("$.estadoAnterior").doesNotExist())
                .andExpect(jsonPath("$.idEmpleado").doesNotExist())
                .andExpect(jsonPath("$.fecha").doesNotExist())
                .andRespond(withStatus(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"id\":1,\"fecha\":\"2026-11-12T12:30:00\"}"));

        adapter.registrarCambioEstado(new RegistroTrazabilidad(
                50L, 7L, 1L, null, null, EstadoPedido.PENDIENTE));

        servidor.verify();
    }

    @Test
    void cambioDeEstadoConEmpleado_enviaLosEstadosComoTexto() {
        servidor.expect(requestTo(BASE_URL + "/trazabilidad"))
                .andExpect(jsonPath("$.idEmpleado").value(9))
                .andExpect(jsonPath("$.estadoAnterior").value("PENDIENTE"))
                .andExpect(jsonPath("$.estadoNuevo").value("EN_PREPARACION"))
                .andRespond(withStatus(HttpStatus.CREATED).contentType(MediaType.APPLICATION_JSON).body("{}"));

        adapter.registrarCambioEstado(new RegistroTrazabilidad(
                50L, 7L, 1L, 9L, EstadoPedido.PENDIENTE, EstadoPedido.EN_PREPARACION));

        servidor.verify();
    }

    @Test
    void servidorConError500_lanzaServicioNoDisponible() {
        servidor.expect(requestTo(BASE_URL + "/trazabilidad")).andRespond(withServerError());

        assertThrows(ServicioTrazabilidadNoDisponibleException.class, () -> adapter.registrarCambioEstado(
                new RegistroTrazabilidad(50L, 7L, 1L, null, null, EstadoPedido.PENDIENTE)));
    }

    @Test
    void rolSinPermiso403_tambienLanzaServicioNoDisponible() {
        servidor.expect(requestTo(BASE_URL + "/trazabilidad")).andRespond(withStatus(HttpStatus.FORBIDDEN));

        assertThrows(ServicioTrazabilidadNoDisponibleException.class, () -> adapter.registrarCambioEstado(
                new RegistroTrazabilidad(50L, 7L, 1L, null, null, EstadoPedido.PENDIENTE)));
    }
}