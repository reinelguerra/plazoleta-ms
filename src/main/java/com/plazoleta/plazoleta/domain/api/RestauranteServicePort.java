package com.plazoleta.plazoleta.domain.api;

import com.plazoleta.plazoleta.domain.model.Pagina;
import com.plazoleta.plazoleta.domain.model.Restaurante;


public interface RestauranteServicePort {

    Restaurante crearRestaurante(Restaurante restaurante);

    Pagina<Restaurante> listarRestaurantes(int pagina, int tamanio);
}