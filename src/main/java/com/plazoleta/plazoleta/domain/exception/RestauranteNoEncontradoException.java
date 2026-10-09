package com.plazoleta.plazoleta.domain.exception;

public class RestauranteNoEncontradoException extends DomainException {

    public RestauranteNoEncontradoException(Long id) {
        super("No existe un restaurante con id " + id);
    }
}