package com.plazoleta.plazoleta.domain.spi;

/** Genera el PIN de entrega de un pedido: 6 dígitos. */
public interface GeneradorPinPort {

    String generar();
}