package com.plazoleta.plazoleta.domain.exception;

/**
 * Excepción base del dominio. Se lanza cuando se rompe una regla de negocio.
 * No depende de Spring ni de HTTP. Más adelante, en infrastructure/exception,
 * un manejador global la convertirá en una respuesta 400.
 */
public class DomainException extends RuntimeException {

    public DomainException(String message) {
        super(message);
    }
}