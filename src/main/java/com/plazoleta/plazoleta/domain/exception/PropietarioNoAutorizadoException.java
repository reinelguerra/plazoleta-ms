package com.plazoleta.plazoleta.domain.exception;

/** Quien hace la petición no es el propietario del restaurante al que pertenece el plato. */
public class PropietarioNoAutorizadoException extends DomainException {

    public PropietarioNoAutorizadoException() {
        super("Solo el propietario del restaurante puede modificar los platos de ese restaurante");
    }
}