package com.plazoleta.plazoleta.infrastructure.output.http.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Lo que responde usuarios-ms en GET /usuarios/{id}.
 * Solo necesitamos el rol; el resto de campos (como apellido) se ignoran.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record UsuarioResponse(Long id, String nombre, String correo, String rol) {
}