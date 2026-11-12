package com.plazoleta.plazoleta.application.handler;

import com.plazoleta.plazoleta.domain.api.RestauranteServicePort;
import com.plazoleta.plazoleta.domain.model.PaginaRespuesta;
import com.plazoleta.plazoleta.domain.model.Restaurante;

public class RestauranteHandlerImpl implements RestauranteHandler {

    private final RestauranteServicePort restauranteServicePort;

    public RestauranteHandlerImpl(RestauranteServicePort restauranteServicePort) {
        this.restauranteServicePort = restauranteServicePort;
    }

    @Override
    public Restaurante crearRestaurante(Restaurante restaurante) {
        return restauranteServicePort.crearRestaurante(restaurante);
    }

    @Override
    public PaginaRespuesta<Restaurante> listarRestaurantes(int pagina, int tamanio) {
        return restauranteServicePort.listarRestaurantes(pagina, tamanio);
    }

    @Override
    public Restaurante obtenerRestaurante(Long id) {
        return restauranteServicePort.obtenerRestaurante(id);
    }
}