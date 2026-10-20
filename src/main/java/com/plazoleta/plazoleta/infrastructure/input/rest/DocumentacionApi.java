package com.plazoleta.plazoleta.infrastructure.input.rest;

public final class DocumentacionApi {

    public static final String NO_AUTENTICADO = "No autenticado: falta el token o no es válido";
    public static final String SIN_PERMISO = "Sin permiso: el rol del token no puede hacer esta acción";
    public static final String SIN_PERMISO_PLATO =
            "Sin permiso: el rol no es PROPIETARIO o el restaurante del plato no es tuyo";
    public static final String DATOS_INVALIDOS = "Datos inválidos o regla de negocio incumplida";
    public static final String PLATO_NO_ENCONTRADO = "No existe un plato con ese id";
    public static final String USUARIOS_NO_DISPONIBLE = "El servicio de usuarios no está disponible";

    private DocumentacionApi() {
    }
}