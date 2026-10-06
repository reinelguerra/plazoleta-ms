package com.plazoleta.plazoleta.domain.exception;

/** El usuario que se quiere asignar como propietario no existe en el servicio de Usuarios. */
public class UsuarioNoEncontradoException extends DomainException {

    public UsuarioNoEncontradoException(Long idUsuario) {
        super("No existe un usuario con id " + idUsuario);
    }
}
