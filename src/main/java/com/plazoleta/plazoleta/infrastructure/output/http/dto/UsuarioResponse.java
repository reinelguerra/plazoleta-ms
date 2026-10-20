package com.plazoleta.plazoleta.infrastructure.output.http.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties(ignoreUnknown = true)
public record UsuarioResponse(Long id, String nombre, String correo, String rol) {
}