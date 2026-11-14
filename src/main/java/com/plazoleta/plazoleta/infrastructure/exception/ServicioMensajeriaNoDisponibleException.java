package com.plazoleta.plazoleta.infrastructure.exception;

public class ServicioMensajeriaNoDisponibleException extends RuntimeException {

    public ServicioMensajeriaNoDisponibleException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}