package com.plazoleta.plazoleta.infrastructure.security;

import com.plazoleta.plazoleta.domain.model.RolUsuario;

public record UsuarioAutenticado(Long id, String correo, RolUsuario rol) {
}