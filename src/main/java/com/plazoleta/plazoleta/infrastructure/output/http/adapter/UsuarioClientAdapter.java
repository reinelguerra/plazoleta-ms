package com.plazoleta.plazoleta.infrastructure.output.http.adapter;

import com.plazoleta.plazoleta.domain.model.RolUsuario;
import com.plazoleta.plazoleta.domain.spi.UsuarioClientPort;
import com.plazoleta.plazoleta.infrastructure.exception.ServicioUsuariosNoDisponibleException;
import com.plazoleta.plazoleta.infrastructure.output.http.dto.UsuarioResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Optional;

/**
 * Adaptador de salida: implementa el puerto del dominio llamando por HTTP a usuarios-ms.
 * Contrato acordado: GET /usuarios/{id} -> 200 con { id, nombre, correo, rol } | 404 si no existe.
 */
@Component
public class UsuarioClientAdapter implements UsuarioClientPort {

    private final RestClient usuariosRestClient;

    public UsuarioClientAdapter(RestClient usuariosRestClient) {
        this.usuariosRestClient = usuariosRestClient;
    }

    @Override
    public Optional<RolUsuario> obtenerRolPorId(Long idUsuario) {
        try {
            UsuarioResponse usuario = usuariosRestClient.get()
                    .uri("/usuarios/{id}", idUsuario)
                    .retrieve()
                    .body(UsuarioResponse.class);

            if (usuario == null || usuario.rol() == null) {
                return Optional.empty();
            }
            return Optional.of(RolUsuario.valueOf(usuario.rol()));

        } catch (HttpClientErrorException.NotFound e) {
            // 404: el usuario no existe
            return Optional.empty();
        } catch (RestClientException e) {
            // servidor caído, 5xx u otro error HTTP inesperado
            throw new ServicioUsuariosNoDisponibleException(
                    "No fue posible consultar el servicio de usuarios", e);
        } catch (IllegalArgumentException e) {
            // el rol que llegó no existe en nuestro enum
            throw new ServicioUsuariosNoDisponibleException(
                    "El servicio de usuarios devolvió un rol desconocido", e);
        }
    }
}