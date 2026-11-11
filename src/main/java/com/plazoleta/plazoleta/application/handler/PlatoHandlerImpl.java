package com.plazoleta.plazoleta.application.handler;

import com.plazoleta.plazoleta.domain.api.PlatoServicePort;
import com.plazoleta.plazoleta.domain.model.PaginaRespuesta;
import com.plazoleta.plazoleta.domain.model.Plato;

public class PlatoHandlerImpl implements PlatoHandler {

    private final PlatoServicePort platoServicePort;

    public PlatoHandlerImpl(PlatoServicePort platoServicePort) {
        this.platoServicePort = platoServicePort;
    }

    @Override
    public Plato crearPlato(Plato plato, Long idPropietario) {
        return platoServicePort.crearPlato(plato, idPropietario);
    }

    @Override
    public Plato modificarPlato(Long idPlato, Integer precio, String descripcion, Long idPropietario) {
        return platoServicePort.modificarPlato(idPlato, precio, descripcion, idPropietario);
    }

    @Override
    public Plato cambiarEstadoPlato(Long idPlato, Boolean activo, Long idPropietario) {
        return platoServicePort.cambiarEstadoPlato(idPlato, activo, idPropietario);
    }

    @Override
    public PaginaRespuesta<Plato> listarPlatos(Long idRestaurante, String categoria, int pagina, int tamanio) {
        return platoServicePort.listarPlatos(idRestaurante, categoria, pagina, tamanio);
    }
}