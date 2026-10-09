package com.plazoleta.plazoleta.domain.api;

import com.plazoleta.plazoleta.domain.model.Pagina;
import com.plazoleta.plazoleta.domain.model.Restaurante;

/**
 * Puerto de entrada: lo que el mundo exterior puede pedirle a la aplicación sobre restaurantes.
 */
public interface RestauranteServicePort {

    Restaurante crearRestaurante(Restaurante restaurante);

    /** HU 9: restaurantes ordenados alfabéticamente por nombre, paginados. */
    Pagina<Restaurante> listarRestaurantes(int pagina, int tamanio);
}