package com.plazoleta.plazoleta.infrastructure.exception;

public class ServicioUsuariosNoDisponibleException extends RuntimeException {

    public ServicioUsuariosNoDisponibleException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}