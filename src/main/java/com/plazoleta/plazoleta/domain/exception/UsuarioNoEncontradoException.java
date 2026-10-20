package com.plazoleta.plazoleta.domain.exception;

public class UsuarioNoEncontradoException extends DomainException {

    public UsuarioNoEncontradoException(Long idUsuario) {
        super("No existe un usuario con id " + idUsuario);
    }
}
