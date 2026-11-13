package com.plazoleta.plazoleta.domain.model;

/** Lo que Plazoleta necesita saber de un usuario de usuarios-ms. idRestaurante solo viene en empleados. */
public record DatosUsuario(Long id, RolUsuario rol, String celular, Long idRestaurante) {
}