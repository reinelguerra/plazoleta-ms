package com.plazoleta.plazoleta.domain.spi;

/** Puerto de salida hacia el microservicio de mensajería. */
public interface MensajeriaClientPort {

    void enviarSms(String celular, String mensaje);
}