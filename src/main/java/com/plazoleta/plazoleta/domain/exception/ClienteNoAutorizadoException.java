package com.plazoleta.plazoleta.domain.exception;

import com.plazoleta.plazoleta.domain.util.MensajeError;

/** El cliente intenta operar sobre un pedido que no es suyo. */
public class ClienteNoAutorizadoException extends RuntimeException {

    public ClienteNoAutorizadoException() {
        super(MensajeError.PEDIDO_DE_OTRO_CLIENTE);
    }
}