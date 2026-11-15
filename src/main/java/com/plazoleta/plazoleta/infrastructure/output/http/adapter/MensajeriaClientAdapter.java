package com.plazoleta.plazoleta.infrastructure.output.http.adapter;

import com.plazoleta.plazoleta.domain.spi.MensajeriaClientPort;
import com.plazoleta.plazoleta.domain.util.MensajeError;
import com.plazoleta.plazoleta.infrastructure.exception.ServicioMensajeriaNoDisponibleException;
import com.plazoleta.plazoleta.infrastructure.output.http.ReenviarAutorizacionInterceptor;
import com.plazoleta.plazoleta.infrastructure.output.http.dto.EnvioSmsRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/** Llama a mensajeria-ms reenviando el token del empleado que hizo la petición (el endpoint exige EMPLEADO). */
@Component
public class MensajeriaClientAdapter implements MensajeriaClientPort {

    private final RestClient restClient;

    @Autowired
    public MensajeriaClientAdapter(
            @Value("${mensajeria.service.url:http://localhost:8083/api/v1}") String baseUrl) {
        this(RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(fabricaConTimeouts())
                .requestInterceptor(new ReenviarAutorizacionInterceptor())
                .build());
    }

    private static org.springframework.http.client.ClientHttpRequestFactory fabricaConTimeouts() {
        org.springframework.http.client.SimpleClientHttpRequestFactory fabrica =
                new org.springframework.http.client.SimpleClientHttpRequestFactory();
        fabrica.setConnectTimeout(java.time.Duration.ofSeconds(3));
        fabrica.setReadTimeout(java.time.Duration.ofSeconds(5));
        return fabrica;
    }

    MensajeriaClientAdapter(RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public void enviarSms(String celular, String mensaje) {
        try {
            restClient.post()
                    .uri("/mensajeria/sms")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new EnvioSmsRequest(celular, mensaje))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException e) {
            throw new ServicioMensajeriaNoDisponibleException(MensajeError.MENSAJERIA_NO_DISPONIBLE, e);
        }
    }
}