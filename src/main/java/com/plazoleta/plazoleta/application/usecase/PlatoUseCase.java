package com.plazoleta.plazoleta.application.usecase;

import com.plazoleta.plazoleta.domain.api.PlatoServicePort;
import com.plazoleta.plazoleta.domain.exception.DomainException;
import com.plazoleta.plazoleta.domain.exception.RestauranteNoEncontradoException;
import com.plazoleta.plazoleta.domain.model.Plato;
import com.plazoleta.plazoleta.domain.spi.PlatoPersistencePort;
import com.plazoleta.plazoleta.domain.spi.RestaurantePersistencePort;

public class PlatoUseCase implements PlatoServicePort {

    private final PlatoPersistencePort platoPersistencePort;
    private final RestaurantePersistencePort restaurantePersistencePort;

    public PlatoUseCase(PlatoPersistencePort platoPersistencePort,
                        RestaurantePersistencePort restaurantePersistencePort) {
        this.platoPersistencePort = platoPersistencePort;
        this.restaurantePersistencePort = restaurantePersistencePort;
    }

    @Override
    public Plato crearPlato(Plato plato) {
        validarPlato(plato);
        restaurantePersistencePort.obtenerRestaurantePorId(plato.getIdRestaurante())
                .orElseThrow(() -> new RestauranteNoEncontradoException(plato.getIdRestaurante()));
        plato.setActivo(true);
        return platoPersistencePort.guardarPlato(plato);
    }

    private void validarPlato(Plato plato) {
        if (plato == null) {
            throw new DomainException("Los datos del plato son obligatorios");
        }
        if (esVacio(plato.getNombre())) {
            throw new DomainException("El nombre del plato es obligatorio");
        }
        if (plato.getPrecio() == null || plato.getPrecio() <= 0) {
            throw new DomainException("El precio es obligatorio y debe ser un número entero mayor a 0");
        }
        if (esVacio(plato.getDescripcion())) {
            throw new DomainException("La descripción es obligatoria");
        }
        if (esVacio(plato.getUrlImagen())) {
            throw new DomainException("La URL de la imagen es obligatoria");
        }
        if (esVacio(plato.getCategoria())) {
            throw new DomainException("La categoría es obligatoria");
        }
        if (plato.getIdRestaurante() == null) {
            throw new DomainException("El restaurante del plato es obligatorio");
        }
    }

    private boolean esVacio(String valor) {
        return valor == null || valor.isBlank();
    }
}