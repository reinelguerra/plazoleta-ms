package com.plazoleta.plazoleta.application.usecase;

import com.plazoleta.plazoleta.domain.api.RestauranteServicePort;
import com.plazoleta.plazoleta.domain.exception.DomainException;
import com.plazoleta.plazoleta.domain.exception.PropietarioInvalidoException;
import com.plazoleta.plazoleta.domain.exception.UsuarioNoEncontradoException;
import com.plazoleta.plazoleta.domain.model.PaginaRespuesta;
import com.plazoleta.plazoleta.domain.model.Restaurante;
import com.plazoleta.plazoleta.domain.model.RolUsuario;
import com.plazoleta.plazoleta.domain.spi.RestaurantePersistencePort;
import com.plazoleta.plazoleta.domain.spi.UsuarioClientPort;
import com.plazoleta.plazoleta.domain.util.MensajeError;
import com.plazoleta.plazoleta.domain.exception.RestauranteNoEncontradoException;

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
        if (restaurante == null) {
            throw new DomainException(MensajeError.RESTAURANTE_OBLIGATORIO);
        }

        Long idPropietario = restaurante.getIdPropietario();
        RolUsuario rol = usuarioClientPort.obtenerRolPorId(idPropietario)
                .orElseThrow(() -> new UsuarioNoEncontradoException(idPropietario));

        if (rol != RolUsuario.PROPIETARIO) {
            throw new PropietarioInvalidoException(idPropietario);
        }

        return restaurantePersistencePort.guardarRestaurante(restaurante);
    }

       @Override
   public PaginaRespuesta<Restaurante> listarRestaurantes(int pagina, int tamanio) {
       if (pagina < 0) {
           throw new DomainException(MensajeError.PAGINA_NEGATIVA);
       }
       if (tamanio < 1) {
           throw new DomainException(MensajeError.TAMANIO_PAGINA_INVALIDO);
       }
       return restaurantePersistencePort.listarRestaurantes(pagina, tamanio);
   }

       @Override
    public Restaurante obtenerRestaurante(Long id) {
        return restaurantePersistencePort.obtenerRestaurantePorId(id)
                .orElseThrow(() -> new RestauranteNoEncontradoException(id));
    }
}
 