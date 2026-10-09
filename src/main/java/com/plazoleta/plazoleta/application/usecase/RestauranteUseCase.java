package com.plazoleta.plazoleta.application.usecase;

import com.plazoleta.plazoleta.domain.api.RestauranteServicePort;
import com.plazoleta.plazoleta.domain.exception.PropietarioInvalidoException;
import com.plazoleta.plazoleta.domain.exception.UsuarioNoEncontradoException;
import com.plazoleta.plazoleta.domain.model.Restaurante;
import com.plazoleta.plazoleta.domain.model.RolUsuario;
import com.plazoleta.plazoleta.domain.spi.RestaurantePersistencePort;
import com.plazoleta.plazoleta.domain.spi.UsuarioClientPort;
import com.plazoleta.plazoleta.domain.validator.RestauranteValidator;

/**
 * Caso de uso: crear un restaurante.
 * Implementa el puerto de entrada y usa los puertos de salida.
 * No lleva anotaciones de Spring: se registrará como bean desde infrastructure/config.
 */
public class RestauranteUseCase implements RestauranteServicePort {

    private final RestaurantePersistencePort restaurantePersistencePort;
    private final UsuarioClientPort usuarioClientPort;

    public RestauranteUseCase(RestaurantePersistencePort restaurantePersistencePort,
                              UsuarioClientPort usuarioClientPort) {
        this.restaurantePersistencePort = restaurantePersistencePort;
        this.usuarioClientPort = usuarioClientPort;
    }

    @Override
    public Restaurante crearRestaurante(Restaurante restaurante) {
        // 1. Reglas de los datos (no necesitan nada externo).
        RestauranteValidator.validar(restaurante);

        // 2. El propietario debe existir en Usuarios y tener rol PROPIETARIO.
        Long idPropietario = restaurante.getIdPropietario();
        RolUsuario rol = usuarioClientPort.obtenerRolPorId(idPropietario)
                .orElseThrow(() -> new UsuarioNoEncontradoException(idPropietario));

        if (rol != RolUsuario.PROPIETARIO) {
            throw new PropietarioInvalidoException(idPropietario);
        }

        // 3. Todo bien: guardar.
        return restaurantePersistencePort.guardarRestaurante(restaurante);
    }
}