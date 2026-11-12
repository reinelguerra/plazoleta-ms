package com.plazoleta.plazoleta.infrastructure.output.http.adapter;

import com.plazoleta.plazoleta.domain.model.EstadoPedido;
import com.plazoleta.plazoleta.domain.model.RegistroTrazabilidad;
import com.plazoleta.plazoleta.domain.spi.TrazabilidadClientPort;
import com.plazoleta.plazoleta.domain.util.MensajeError;
import com.plazoleta.plazoleta.infrastructure.exception.ServicioTrazabilidadNoDisponibleException;
import com.plazoleta.plazoleta.infrastructure.output.http.ReenviarAutorizacionInterceptor;
import com.plazoleta.plazoleta.infrastructure.output.http.dto.LogTrazabilidadRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/** Llama a trazabilidad-ms reenviando el token del usuario que hizo la petición. */
@Component
public class TrazabilidadClientAdapter implements TrazabilidadClientPort {

    private final RestClient restClient;

    @Autowired
    public TrazabilidadClientAdapter(
            @Value("${trazabilidad.service.url:http://localhost:8084/api/v1}") String baseUrl) {
        this(RestClient.builder()
                .baseUrl(baseUrl)
                .requestInterceptor(new ReenviarAutorizacionInterceptor())
                .build());
    }

    TrazabilidadClientAdapter(RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public void registrarCambioEstado(RegistroTrazabilidad registro) {
        LogTrazabilidadRequest cuerpo = new LogTrazabilidadRequest(
                registro.idPedido(), registro.idCliente(), registro.idRestaurante(), registro.idEmpleado(),
                nombre(registro.estadoAnterior()), nombre(registro.estadoNuevo()));
        try {
            restClient.post()
                    .uri("/trazabilidad")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(cuerpo)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException e) {
            throw new ServicioTrazabilidadNoDisponibleException(MensajeError.TRAZABILIDAD_NO_DISPONIBLE, e);
        }
    }

    private static String nombre(EstadoPedido estado) {
        return estado == null ? null : estado.name();
    }
}