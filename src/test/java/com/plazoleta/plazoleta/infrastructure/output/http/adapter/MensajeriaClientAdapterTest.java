package com.plazoleta.plazoleta.infrastructure.output.http.adapter;

import com.plazoleta.plazoleta.domain.util.MensajeError;
import com.plazoleta.plazoleta.infrastructure.exception.ServicioMensajeriaNoDisponibleException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class MensajeriaClientAdapterTest {

    private static final String BASE = "http://localhost:8083/api/v1";

    private MockRestServiceServer servidor;
    private MensajeriaClientAdapter adapter;

    @BeforeEach
    void preparar() {
        RestClient.Builder builder = RestClient.builder().baseUrl(BASE);
        servidor = MockRestServiceServer.bindTo(builder).build();
        adapter = new MensajeriaClientAdapter(builder.build());
    }

    @Test
    void enviarSms_hacePostConCelularYMensaje() {
        servidor.expect(requestTo(BASE + "/mensajeria/sms"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(jsonPath("$.celular").value("+573001234567"))
                .andExpect(jsonPath("$.mensaje").value("Tu pedido 40 esta listo. PIN: 123456"))
                .andRespond(withSuccess("{\"celular\":\"+573001234567\",\"estado\":\"SIMULADO\"}",
                        MediaType.APPLICATION_JSON));

        adapter.enviarSms("+573001234567", "Tu pedido 40 esta listo. PIN: 123456");

        servidor.verify();
    }

    @Test
    void enviarSms_siMensajeriaFalla_lanzaServicioNoDisponible() {
        servidor.expect(requestTo(BASE + "/mensajeria/sms")).andRespond(withServerError());

        ServicioMensajeriaNoDisponibleException e = assertThrows(ServicioMensajeriaNoDisponibleException.class,
                () -> adapter.enviarSms("+573001234567", "Hola"));

        assertEquals(MensajeError.MENSAJERIA_NO_DISPONIBLE, e.getMessage());
    }
}