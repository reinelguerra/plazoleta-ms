package com.plazoleta.plazoleta.domain.spi;

import com.plazoleta.plazoleta.domain.model.RolUsuario;

import java.util.Optional;


public interface UsuarioClientPort {

    Optional<RolUsuario> obtenerRolPorId(Long idUsuario);
}