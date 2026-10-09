package com.plazoleta.plazoleta.infrastructure.exception;

/**
 * Usuarios no respondió como se esperaba (caído, error 5xx o respuesta inválida).
 * Es una excepción de infraestructura, no de negocio: por eso NO extiende DomainException.
 */
public class ServicioUsuariosNoDisponibleException extends RuntimeException {

    public ServicioUsuariosNoDisponibleException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}