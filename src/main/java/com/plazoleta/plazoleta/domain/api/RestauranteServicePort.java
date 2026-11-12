package com.plazoleta.plazoleta.domain.api;

import com.plazoleta.plazoleta.domain.model.PaginaRespuesta;
import com.plazoleta.plazoleta.domain.model.Restaurante;


public interface RestauranteServicePort {

    Restaurante crearRestaurante(Restaurante restaurante);

    PaginaRespuesta<Restaurante> listarRestaurantes(int PaginaRespuesta, int tamanio);
    
    Restaurante obtenerRestaurante(Long id);
}
