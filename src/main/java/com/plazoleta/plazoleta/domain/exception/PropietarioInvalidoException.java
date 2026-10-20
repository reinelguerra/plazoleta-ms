package com.plazoleta.plazoleta.domain.exception;

public class PropietarioInvalidoException extends DomainException {

    public PropietarioInvalidoException(Long idUsuario) {
        super("El usuario con id " + idUsuario + " no tiene rol de propietario");
    }
}