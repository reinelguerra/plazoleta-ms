package com.plazoleta.plazoleta.application.usecase;

import com.plazoleta.plazoleta.domain.exception.DomainException;
import com.plazoleta.plazoleta.domain.exception.PedidoNoEncontradoException;
import com.plazoleta.plazoleta.domain.exception.UsuarioNoEncontradoException;
import com.plazoleta.plazoleta.domain.model.DatosUsuario;
import com.plazoleta.plazoleta.domain.model.EstadoPedido;
import com.plazoleta.plazoleta.domain.model.Pedido;
import com.plazoleta.plazoleta.domain.model.RegistroTrazabilidad;
import com.plazoleta.plazoleta.domain.model.RolUsuario;
import com.plazoleta.plazoleta.domain.spi.PedidoPersistencePort;
import com.plazoleta.plazoleta.domain.spi.PlatoPersistencePort;
import com.plazoleta.plazoleta.domain.spi.RestaurantePersistencePort;
import com.plazoleta.plazoleta.domain.spi.TrazabilidadClientPort;
import com.plazoleta.plazoleta.domain.spi.UsuarioClientPort;
import com.plazoleta.plazoleta.domain.util.MensajeError;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/** HU 15: el empleado entrega un pedido LISTO validando el PIN que recibió el cliente. */
@ExtendWith(MockitoExtension.class)
class PedidoUseCaseEntregaTest {

    private static final Long ID_PEDIDO = 40L;
    private static final Long ID_EMPLEADO = 12L;
    private static final Long ID_CLIENTE = 7L;
    private static final Long ID_RESTAURANTE = 3L;
    private static final String PIN = "123456";

    @Mock
    private PedidoPersistencePort pedidoPersistencePort;

    @Mock
    private PlatoPersistencePort platoPersistencePort;

    @Mock
    private RestaurantePersistencePort restaurantePersistencePort;

    @Mock
    private TrazabilidadClientPort trazabilidadClientPort;

    @Mock
    private UsuarioClientPort usuarioClientPort;

    private PedidoUseCase pedidoUseCase;

    @BeforeEach
    void preparar() {
        pedidoUseCase = new PedidoUseCase(pedidoPersistencePort, platoPersistencePort,
                restaurantePersistencePort, trazabilidadClientPort, usuarioClientPort,
                null, null);
    }

    private void empleadoConRestaurante(Long idRestaurante) {
        when(usuarioClientPort.obtenerUsuarioPorId(ID_EMPLEADO)).thenReturn(Optional.of(
                new DatosUsuario(ID_EMPLEADO, RolUsuario.EMPLEADO, "+573001112233", idRestaurante)));
    }

    private Pedido pedido(Long idRestaurante, EstadoPedido estado) {
        Pedido pedido = new Pedido();
        pedido.setId(ID_PEDIDO);
        pedido.setIdCliente(ID_CLIENTE);
        pedido.setIdRestaurante(idRestaurante);
        pedido.setIdEmpleado(ID_EMPLEADO);
        pedido.setEstado(estado);
        pedido.setPin(PIN);
        return pedido;
    }

    @Test
    void pedidoListoConPinCorrecto_pasaAEntregadoYRegistraElLog() {
        empleadoConRestaurante(ID_RESTAURANTE);
        when(pedidoPersistencePort.obtenerPedidoPorId(ID_PEDIDO))
                .thenReturn(Optional.of(pedido(ID_RESTAURANTE, EstadoPedido.LISTO)));
        when(pedidoPersistencePort.guardarPedido(any(Pedido.class))).thenAnswer(i -> i.getArgument(0));

        Pedido resultado = pedidoUseCase.entregarPedido(ID_PEDIDO, ID_EMPLEADO, PIN);

        assertEquals(EstadoPedido.ENTREGADO, resultado.getEstado());

        ArgumentCaptor<RegistroTrazabilidad> captor = ArgumentCaptor.forClass(RegistroTrazabilidad.class);
        verify(trazabilidadClientPort).registrarCambioEstado(captor.capture());
        RegistroTrazabilidad registro = captor.getValue();
        assertEquals(ID_PEDIDO, registro.idPedido());
        assertEquals(ID_CLIENTE, registro.idCliente());
        assertEquals(ID_RESTAURANTE, registro.idRestaurante());
        assertEquals(ID_EMPLEADO, registro.idEmpleado());
        assertEquals(EstadoPedido.LISTO, registro.estadoAnterior());
        assertEquals(EstadoPedido.ENTREGADO, registro.estadoNuevo());
    }

    @Test
    void pinIncorrecto_lanzaExcepcionSinGuardarNiRegistrar() {
        empleadoConRestaurante(ID_RESTAURANTE);
        when(pedidoPersistencePort.obtenerPedidoPorId(ID_PEDIDO))
                .thenReturn(Optional.of(pedido(ID_RESTAURANTE, EstadoPedido.LISTO)));

        DomainException e = assertThrows(DomainException.class,
                () -> pedidoUseCase.entregarPedido(ID_PEDIDO, ID_EMPLEADO, "000000"));

        assertEquals(MensajeError.PIN_INCORRECTO, e.getMessage());
        verify(pedidoPersistencePort, never()).guardarPedido(any());
        verifyNoInteractions(trazabilidadClientPort);
    }

    @Test
    void pinNulo_seTrataComoIncorrecto() {
        empleadoConRestaurante(ID_RESTAURANTE);
        when(pedidoPersistencePort.obtenerPedidoPorId(ID_PEDIDO))
                .thenReturn(Optional.of(pedido(ID_RESTAURANTE, EstadoPedido.LISTO)));

        DomainException e = assertThrows(DomainException.class,
                () -> pedidoUseCase.entregarPedido(ID_PEDIDO, ID_EMPLEADO, null));

        assertEquals(MensajeError.PIN_INCORRECTO, e.getMessage());
        verify(pedidoPersistencePort, never()).guardarPedido(any());
        verifyNoInteractions(trazabilidadClientPort);
    }

    @Test
    void pedidoQueNoEstaListo_lanzaExcepcionAunqueElPinCoincida() {
        for (EstadoPedido estado : new EstadoPedido[]{EstadoPedido.PENDIENTE, EstadoPedido.EN_PREPARACION,
                EstadoPedido.ENTREGADO, EstadoPedido.CANCELADO}) {
            empleadoConRestaurante(ID_RESTAURANTE);
            when(pedidoPersistencePort.obtenerPedidoPorId(ID_PEDIDO))
                    .thenReturn(Optional.of(pedido(ID_RESTAURANTE, estado)));

            DomainException e = assertThrows(DomainException.class,
                    () -> pedidoUseCase.entregarPedido(ID_PEDIDO, ID_EMPLEADO, PIN));

            assertEquals(MensajeError.PEDIDO_NO_LISTO, e.getMessage());
        }
        verify(pedidoPersistencePort, never()).guardarPedido(any());
        verifyNoInteractions(trazabilidadClientPort);
    }

    @Test
    void pedidoDeOtroRestaurante_lanzaExcepcionSinGuardarNiRegistrar() {
        empleadoConRestaurante(ID_RESTAURANTE);
        when(pedidoPersistencePort.obtenerPedidoPorId(ID_PEDIDO))
                .thenReturn(Optional.of(pedido(99L, EstadoPedido.LISTO)));

        DomainException e = assertThrows(DomainException.class,
                () -> pedidoUseCase.entregarPedido(ID_PEDIDO, ID_EMPLEADO, PIN));

        assertEquals(MensajeError.PEDIDO_DE_OTRO_RESTAURANTE, e.getMessage());
        verify(pedidoPersistencePort, never()).guardarPedido(any());
        verifyNoInteractions(trazabilidadClientPort);
    }

    @Test
    void empleadoInexistente_lanzaNoEncontradoSinTocarElPedido() {
        when(usuarioClientPort.obtenerUsuarioPorId(ID_EMPLEADO)).thenReturn(Optional.empty());

        assertThrows(UsuarioNoEncontradoException.class,
                () -> pedidoUseCase.entregarPedido(ID_PEDIDO, ID_EMPLEADO, PIN));

        verifyNoInteractions(pedidoPersistencePort, trazabilidadClientPort);
    }

    @Test
    void empleadoSinRestaurante_lanzaExcepcionSinTocarElPedido() {
        empleadoConRestaurante(null);

        DomainException e = assertThrows(DomainException.class,
                () -> pedidoUseCase.entregarPedido(ID_PEDIDO, ID_EMPLEADO, PIN));

        assertEquals(MensajeError.EMPLEADO_SIN_RESTAURANTE, e.getMessage());
        verifyNoInteractions(pedidoPersistencePort, trazabilidadClientPort);
    }

    @Test
    void pedidoInexistente_lanzaNoEncontrado() {
        empleadoConRestaurante(ID_RESTAURANTE);
        when(pedidoPersistencePort.obtenerPedidoPorId(ID_PEDIDO)).thenReturn(Optional.empty());

        assertThrows(PedidoNoEncontradoException.class,
                () -> pedidoUseCase.entregarPedido(ID_PEDIDO, ID_EMPLEADO, PIN));

        verify(pedidoPersistencePort, never()).guardarPedido(any());
        verifyNoInteractions(trazabilidadClientPort);
    }
}