package com.plazoleta.plazoleta.application.usecase;

import com.plazoleta.plazoleta.domain.api.PlatoServicePort;
import com.plazoleta.plazoleta.domain.exception.DomainException;
import com.plazoleta.plazoleta.domain.exception.PlatoNoEncontradoException;
import com.plazoleta.plazoleta.domain.exception.PropietarioNoAutorizadoException;
import com.plazoleta.plazoleta.domain.exception.RestauranteNoEncontradoException;
import com.plazoleta.plazoleta.domain.model.PaginaRespuesta;
import com.plazoleta.plazoleta.domain.model.Plato;
import com.plazoleta.plazoleta.domain.model.Restaurante;
import com.plazoleta.plazoleta.domain.spi.PlatoPersistencePort;
import com.plazoleta.plazoleta.domain.spi.RestaurantePersistencePort;
import com.plazoleta.plazoleta.domain.util.MensajeError;

/**
 * Solo reglas de negocio de los platos: el restaurante y el plato existen y el propietario es el dueño.
 * La forma de los datos (obligatorios, precio entero mayor a 0) se valida en los DTOs de entrada.
 */
public class PlatoUseCase implements PlatoServicePort {

    private final PlatoPersistencePort platoPersistencePort;
    private final RestaurantePersistencePort restaurantePersistencePort;

    public PlatoUseCase(PlatoPersistencePort platoPersistencePort,
                        RestaurantePersistencePort restaurantePersistencePort) {
        this.platoPersistencePort = platoPersistencePort;
        this.restaurantePersistencePort = restaurantePersistencePort;
    }

    @Override
    public Plato crearPlato(Plato plato, Long idPropietario) {
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
        Plato plato = obtenerPlatoDelPropietario(idPlato, idPropietario);

        plato.setPrecio(precio);
        plato.setDescripcion(descripcion);
        return platoPersistencePort.guardarPlato(plato);
    }

    @Override
    public Plato cambiarEstadoPlato(Long idPlato, Boolean activo, Long idPropietario) {
        Plato plato = obtenerPlatoDelPropietario(idPlato, idPropietario);
        plato.setActivo(activo);
        return platoPersistencePort.guardarPlato(plato);
    }

    @Override
    public PaginaRespuesta<Plato> listarPlatos(Long idRestaurante, String categoria, int pagina, int tamanio) {
        if (idRestaurante == null) {
            throw new DomainException(MensajeError.RESTAURANTE_OBLIGATORIO);
        }
        if (pagina < 0) {
            throw new DomainException(MensajeError.PAGINA_NEGATIVA);
        }
        if (tamanio < 1) {
            throw new DomainException(MensajeError.TAMANIO_PAGINA_INVALIDO);
        }
        restaurantePersistencePort.obtenerRestaurantePorId(idRestaurante)
                .orElseThrow(() -> new RestauranteNoEncontradoException(idRestaurante));

        String filtro = esVacio(categoria) ? null : categoria.trim();
        return platoPersistencePort.listarPlatosActivosPorRestaurante(idRestaurante, filtro, pagina, tamanio);
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

    private boolean esVacio(String valor) {
        return valor == null || valor.isBlank();
    }
}