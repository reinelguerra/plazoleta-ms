package com.plazoleta.plazoleta.infrastructure.exception;

/** Formato de error acordado con Reinel para los dos servicios: { "mensaje": "...", "status": 400 }. */
public record ErrorResponse(String mensaje, int status) {
}