package com.plazoleta.plazoleta.infrastructure.output.http.dto;

/** Cuerpo del POST /mensajeria/sms (contrato acordado con mensajeria-ms). */
public record EnvioSmsRequest(String celular, String mensaje) {
}