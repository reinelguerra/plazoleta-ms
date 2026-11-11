package com.plazoleta.plazoleta.application.handler;

import com.plazoleta.plazoleta.domain.model.PaginaRespuesta;
import com.plazoleta.plazoleta.domain.model.Restaurante;

/** Puerto de entrada de la capa de aplicación: lo único que conoce el controller. */
public interface RestauranteHandler {

    Restaurante crearRestaurante(Restaurante restaurante);

    PaginaRespuesta<Restaurante> listarRestaurantes(int pagina, int tamanio);
}