package com.plazoleta.plazoleta.domain.exception;

/** El plato que se quiere modificar no existe. */
public class PlatoNoEncontradoException extends DomainException {

    public PlatoNoEncontradoException(Long idPlato) {
        super("No existe un plato con id " + idPlato);
    }
}