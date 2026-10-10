package com.plazoleta.plazoleta.infrastructure.security;

import com.plazoleta.plazoleta.domain.model.RolUsuario;

/** Quién hace la petición, según lo que dice el token ya verificado. */
public record UsuarioAutenticado(Long id, String correo, RolUsuario rol) {
}