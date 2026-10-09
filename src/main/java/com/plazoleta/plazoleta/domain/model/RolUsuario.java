package com.plazoleta.plazoleta.domain.model;

/**
 * Roles que existen en el sistema. Este servicio no administra usuarios:
 * solo necesita conocer el rol para decidir si puede ser propietario.
 */
public enum RolUsuario {
    ADMINISTRADOR,
    PROPIETARIO,
    EMPLEADO,
    CLIENTE
}