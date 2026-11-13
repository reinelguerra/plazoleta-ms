package com.plazoleta.plazoleta.domain.exception;

import com.plazoleta.plazoleta.domain.util.MensajeError;

public class PedidoNoEncontradoException extends RuntimeException {

    public PedidoNoEncontradoException(Long idPedido) {
        super(MensajeError.PEDIDO_NO_ENCONTRADO + idPedido);
    }
}