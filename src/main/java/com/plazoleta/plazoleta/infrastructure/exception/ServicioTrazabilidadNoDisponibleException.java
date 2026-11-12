package com.plazoleta.plazoleta.infrastructure.exception;

public class ServicioTrazabilidadNoDisponibleException extends RuntimeException {

    public ServicioTrazabilidadNoDisponibleException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}