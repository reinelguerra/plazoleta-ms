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
            return Optional.empty();
        } catch (RestClientException e) {
            throw new ServicioUsuariosNoDisponibleException(
                    "No fue posible consultar el servicio de usuarios", e);
        } catch (IllegalArgumentException e) {
            throw new ServicioUsuariosNoDisponibleException(
                    "El servicio de usuarios devolvió un rol desconocido", e);
        }
    }
}