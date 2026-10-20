package com.plazoleta.plazoleta.domain.exception;

public class PlatoNoEncontradoException extends DomainException {

    public PlatoNoEncontradoException(Long idPlato) {
        super("No existe un plato con id " + idPlato);
    }
}