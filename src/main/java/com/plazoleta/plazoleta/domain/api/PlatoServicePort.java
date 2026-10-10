package com.plazoleta.plazoleta.domain.api;

import com.plazoleta.plazoleta.domain.model.Plato;

/**
 * Puerto de entrada: lo que el mundo exterior puede pedirle a la aplicación sobre los platos.
 */
public interface PlatoServicePort {

    Plato crearPlato(Plato plato);

    /** HU 4: solo se pueden modificar el precio y la descripción, y solo por el propietario del restaurante. */
    Plato modificarPlato(Long idPlato, Integer precio, String descripcion, Long idPropietario);

        /** HU 7: habilita (true) o deshabilita (false) un plato; solo el propietario de su restaurante. */
    Plato cambiarEstadoPlato(Long idPlato, Boolean activo, Long idPropietario);
}