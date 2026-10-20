package com.plazoleta.plazoleta.application.usecase;

import com.plazoleta.plazoleta.domain.api.PlatoServicePort;
import com.plazoleta.plazoleta.domain.exception.DomainException;
import com.plazoleta.plazoleta.domain.exception.PlatoNoEncontradoException;
import com.plazoleta.plazoleta.domain.exception.PropietarioNoAutorizadoException;
import com.plazoleta.plazoleta.domain.exception.RestauranteNoEncontradoException;
import com.plazoleta.plazoleta.domain.model.Plato;
import com.plazoleta.plazoleta.domain.model.Restaurante;
import com.plazoleta.plazoleta.domain.spi.PlatoPersistencePort;
import com.plazoleta.plazoleta.domain.spi.RestaurantePersistencePort;

public class PlatoUseCase implements PlatoServicePort {

    private static final String MSG_PRECIO_INVALIDO = "El precio es obligatorio y debe ser un número entero mayor a 0";
    private static final String MSG_DESCRIPCION_OBLIGATORIA = "La descripción es obligatoria";
    private static final String MSG_ESTADO_OBLIGATORIO = "El estado del plato es obligatorio";

    private final PlatoPersistencePort platoPersistencePort;
    private final RestaurantePersistencePort restaurantePersistencePort;

    public PlatoUseCase(PlatoPersistencePort platoPersistencePort,
                        RestaurantePersistencePort restaurantePersistencePort) {
        this.platoPersistencePort = platoPersistencePort;
        this.restaurantePersistencePort = restaurantePersistencePort;
    }

    @Override
    public Plato crearPlato(Plato plato, Long idPropietario) {
        validarPlato(plato);
        Restaurante restaurante = restaurantePersistencePort.obtenerRestaurantePorId(plato.getIdRestaurante())
                .orElseThrow(() -> new RestauranteNoEncontradoException(plato.getIdRestaurante()));
        if (idPropietario == null || !idPropietario.equals(restaurante.getIdPropietario())) {
            throw new PropietarioNoAutorizadoException();
        }
        plato.setActivo(true);
        return platoPersistencePort.guardarPlato(plato);
    }

    @Override
    public Plato modificarPlato(Long idPlato, Integer precio, String descripcion, Long idPropietario) {
        validarPrecio(precio);
        validarDescripcion(descripcion);

        Plato plato = obtenerPlatoDelPropietario(idPlato, idPropietario);

        plato.setPrecio(precio);
        plato.setDescripcion(descripcion);
        return platoPersistencePort.guardarPlato(plato);
    }
        @Override
    public Plato cambiarEstadoPlato(Long idPlato, Boolean activo, Long idPropietario) {
        if (activo == null) {
            throw new DomainException(MSG_ESTADO_OBLIGATORIO);
        }

        Plato plato = obtenerPlatoDelPropietario(idPlato, idPropietario);
        plato.setActivo(activo);
        return platoPersistencePort.guardarPlato(plato);
    }

    private Plato obtenerPlatoDelPropietario(Long idPlato, Long idPropietario) {
        Plato plato = platoPersistencePort.obtenerPlatoPorId(idPlato)
                .orElseThrow(() -> new PlatoNoEncontradoException(idPlato));
        Restaurante restaurante = restaurantePersistencePort.obtenerRestaurantePorId(plato.getIdRestaurante())
                .orElseThrow(() -> new RestauranteNoEncontradoException(plato.getIdRestaurante()));

        if (idPropietario == null || !idPropietario.equals(restaurante.getIdPropietario())) {
            throw new PropietarioNoAutorizadoException();
        }
        return plato;
    }

    private void validarPlato(Plato plato) {
        if (plato == null) {
            throw new DomainException("Los datos del plato son obligatorios");
        }
        if (esVacio(plato.getNombre())) {
            throw new DomainException("El nombre del plato es obligatorio");
        }
        validarPrecio(plato.getPrecio());
        validarDescripcion(plato.getDescripcion());
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

    private void validarPrecio(Integer precio) {
        if (precio == null || precio <= 0) {
            throw new DomainException(MSG_PRECIO_INVALIDO);
        }
    }

    private void validarDescripcion(String descripcion) {
        if (esVacio(descripcion)) {
            throw new DomainException(MSG_DESCRIPCION_OBLIGATORIA);
        }
    }

    private boolean esVacio(String valor) {
        return valor == null || valor.isBlank();
    }
}