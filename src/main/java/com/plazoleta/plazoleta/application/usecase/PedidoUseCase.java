package com.plazoleta.plazoleta.application.usecase;

import com.plazoleta.plazoleta.domain.api.PedidoServicePort;
import com.plazoleta.plazoleta.domain.exception.DomainException;
import com.plazoleta.plazoleta.domain.exception.PlatoNoEncontradoException;
import com.plazoleta.plazoleta.domain.exception.RestauranteNoEncontradoException;
import com.plazoleta.plazoleta.domain.exception.UsuarioNoEncontradoException;
import com.plazoleta.plazoleta.domain.model.DatosUsuario;
import com.plazoleta.plazoleta.domain.model.EstadoPedido;
import com.plazoleta.plazoleta.domain.model.PaginaRespuesta;
import com.plazoleta.plazoleta.domain.model.Pedido;
import com.plazoleta.plazoleta.domain.model.PedidoPlato;
import com.plazoleta.plazoleta.domain.model.Plato;
import com.plazoleta.plazoleta.domain.model.RegistroTrazabilidad;
import com.plazoleta.plazoleta.domain.spi.PedidoPersistencePort;
import com.plazoleta.plazoleta.domain.spi.PlatoPersistencePort;
import com.plazoleta.plazoleta.domain.spi.RestaurantePersistencePort;
import com.plazoleta.plazoleta.domain.spi.TrazabilidadClientPort;
import com.plazoleta.plazoleta.domain.spi.UsuarioClientPort;
import com.plazoleta.plazoleta.domain.util.MensajeError;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PedidoUseCase implements PedidoServicePort {

    private final PedidoPersistencePort pedidoPersistencePort;
    private final PlatoPersistencePort platoPersistencePort;
    private final RestaurantePersistencePort restaurantePersistencePort;
    private final TrazabilidadClientPort trazabilidadClientPort;
    private final UsuarioClientPort usuarioClientPort;

    public PedidoUseCase(PedidoPersistencePort pedidoPersistencePort,
                         PlatoPersistencePort platoPersistencePort,
                         RestaurantePersistencePort restaurantePersistencePort,
                         TrazabilidadClientPort trazabilidadClientPort,
                         UsuarioClientPort usuarioClientPort) {
        this.pedidoPersistencePort = pedidoPersistencePort;
        this.platoPersistencePort = platoPersistencePort;
        this.restaurantePersistencePort = restaurantePersistencePort;
        this.trazabilidadClientPort = trazabilidadClientPort;
        this.usuarioClientPort = usuarioClientPort;
    }

    @Override
    public Pedido realizarPedido(Pedido pedido) {
        validarDatos(pedido);

        restaurantePersistencePort.obtenerRestaurantePorId(pedido.getIdRestaurante())
                .orElseThrow(() -> new RestauranteNoEncontradoException(pedido.getIdRestaurante()));

        if (pedidoPersistencePort.existePedidoDeClienteEnEstados(pedido.getIdCliente(), EstadoPedido.EN_PROCESO)) {
            throw new DomainException(MensajeError.CLIENTE_CON_PEDIDO_EN_PROCESO);
        }

        validarPlatos(pedido);

        pedido.setEstado(EstadoPedido.PENDIENTE);
        pedido.setIdEmpleado(null);
        pedido.setPin(null);
        pedido.setFecha(LocalDateTime.now());
        Pedido guardado = pedidoPersistencePort.guardarPedido(pedido);

        trazabilidadClientPort.registrarCambioEstado(new RegistroTrazabilidad(
                guardado.getId(), guardado.getIdCliente(), guardado.getIdRestaurante(),
                guardado.getIdEmpleado(), null, guardado.getEstado()));

        return guardado;
    }

    @Override
    public PaginaRespuesta<Pedido> listarPedidosPorEstado(Long idEmpleado, EstadoPedido estado,
                                                          int pagina, int tamanio) {
        if (estado == null) {
            throw new DomainException(MensajeError.ESTADO_PEDIDO_OBLIGATORIO);
        }
        if (pagina < 0) {
            throw new DomainException(MensajeError.PAGINA_NEGATIVA);
        }
        if (tamanio < 1) {
            throw new DomainException(MensajeError.TAMANIO_PAGINA_INVALIDO);
        }

        DatosUsuario empleado = usuarioClientPort.obtenerUsuarioPorId(idEmpleado)
                .orElseThrow(() -> new UsuarioNoEncontradoException(idEmpleado));
        if (empleado.idRestaurante() == null) {
            throw new DomainException(MensajeError.EMPLEADO_SIN_RESTAURANTE);
        }

        return pedidoPersistencePort.listarPedidosPorRestauranteYEstado(
                empleado.idRestaurante(), estado, pagina, tamanio);
    }

    private void validarDatos(Pedido pedido) {
        if (pedido == null) {
            throw new DomainException(MensajeError.PEDIDO_OBLIGATORIO);
        }
        if (pedido.getIdCliente() == null) {
            throw new DomainException(MensajeError.CLIENTE_OBLIGATORIO);
        }
        if (pedido.getIdRestaurante() == null) {
            throw new DomainException(MensajeError.RESTAURANTE_OBLIGATORIO);
        }
        List<PedidoPlato> platos = pedido.getPlatos();
        if (platos == null || platos.isEmpty()) {
            throw new DomainException(MensajeError.PEDIDO_SIN_PLATOS);
        }
        Set<Long> vistos = new HashSet<>();
        for (PedidoPlato item : platos) {
            if (item == null || item.getIdPlato() == null) {
                throw new DomainException(MensajeError.PLATO_PEDIDO_INVALIDO);
            }
            if (item.getCantidad() == null || item.getCantidad() <= 0) {
                throw new DomainException(MensajeError.CANTIDAD_INVALIDA);
            }
            if (!vistos.add(item.getIdPlato())) {
                throw new DomainException(MensajeError.PLATO_REPETIDO);
            }
        }
    }

    private void validarPlatos(Pedido pedido) {
        for (PedidoPlato item : pedido.getPlatos()) {
            Plato plato = platoPersistencePort.obtenerPlatoPorId(item.getIdPlato())
                    .orElseThrow(() -> new PlatoNoEncontradoException(item.getIdPlato()));
            if (!pedido.getIdRestaurante().equals(plato.getIdRestaurante())) {
                throw new DomainException(MensajeError.PLATO_DE_OTRO_RESTAURANTE);
            }
            if (!Boolean.TRUE.equals(plato.getActivo())) {
                throw new DomainException(MensajeError.PLATO_INACTIVO);
            }
        }
    }
}