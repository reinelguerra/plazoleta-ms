package com.plazoleta.plazoleta.domain.spi;

import com.plazoleta.plazoleta.domain.model.RolUsuario;

import java.util.Optional;

/**
 * Puerto de salida: lo que la aplicación necesita saber del microservicio de Usuarios.
 * Devuelve vacío si el usuario no existe.
 * Cómo se consulta (HTTP, otro servicio, datos de prueba) es asunto del adaptador.
 */
public interface UsuarioClientPort {

    Optional<RolUsuario> obtenerRolPorId(Long idUsuario);
}