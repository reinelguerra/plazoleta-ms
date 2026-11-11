package com.plazoleta.plazoleta.application.handler;

import com.plazoleta.plazoleta.domain.model.PaginaRespuesta;
import com.plazoleta.plazoleta.domain.model.Plato;

/** Puerto de entrada de la capa de aplicación para platos: lo único que conoce el controller. */
public interface PlatoHandler {

    Plato crearPlato(Plato plato, Long idPropietario);

    Plato modificarPlato(Long idPlato, Integer precio, String descripcion, Long idPropietario);

    Plato cambiarEstadoPlato(Long idPlato, Boolean activo, Long idPropietario);

    PaginaRespuesta<Plato> listarPlatos(Long idRestaurante, String categoria, int pagina, int tamanio);
}