package com.plazoleta.plazoleta.domain.api;

import com.plazoleta.plazoleta.domain.model.Restaurante;

/**
 * Puerto de entrada: lo que el mundo exterior puede pedirle a la aplicación.
 * El controlador REST (mañana) hablará con esta interfaz, nunca con el caso de uso directamente.
 */
public interface RestauranteServicePort {

    Restaurante crearRestaurante(Restaurante restaurante);
}