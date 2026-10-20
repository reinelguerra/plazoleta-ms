package com.plazoleta.plazoleta.domain.exception;

public class PropietarioNoAutorizadoException extends DomainException {

    public PropietarioNoAutorizadoException() {
        super("Solo el propietario del restaurante puede modificar los platos de ese restaurante");
    }
}