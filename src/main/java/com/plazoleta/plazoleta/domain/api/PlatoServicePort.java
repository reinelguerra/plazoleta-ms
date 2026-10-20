package com.plazoleta.plazoleta.domain.api;

import com.plazoleta.plazoleta.domain.model.Plato;


public interface PlatoServicePort {

    Plato crearPlato(Plato plato, Long idPropietario);

    Plato modificarPlato(Long idPlato, Integer precio, String descripcion, Long idPropietario);

    Plato cambiarEstadoPlato(Long idPlato, Boolean activo, Long idPropietario);
}