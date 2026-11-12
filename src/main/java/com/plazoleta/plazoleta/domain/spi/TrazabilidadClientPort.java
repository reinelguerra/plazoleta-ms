package com.plazoleta.plazoleta.domain.spi;

import com.plazoleta.plazoleta.domain.model.RegistroTrazabilidad;

/** Puerto de salida hacia el microservicio de trazabilidad. */
public interface TrazabilidadClientPort {

    void registrarCambioEstado(RegistroTrazabilidad registro);
}