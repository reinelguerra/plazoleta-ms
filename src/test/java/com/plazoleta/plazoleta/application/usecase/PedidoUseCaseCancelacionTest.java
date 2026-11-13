package com.plazoleta.plazoleta.application.usecase;

import com.plazoleta.plazoleta.domain.exception.ClienteNoAutorizadoException;
import com.plazoleta.plazoleta.domain.exception.DomainException;
import com.plazoleta.plazoleta.domain.exception.PedidoNoEncontradoException;
import com.plazoleta.plazoleta.domain.model.EstadoPedido;
import com.plazoleta.plazoleta.domain.model.Pedido;
import com.plazoleta.plazoleta.domain.model.RegistroTrazabilidad;
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/** HU 16: solo el cliente dueño cancela, y solo un pedido PENDIENTE. */
@ExtendWith(MockitoExtension.class)
class PedidoUseCaseCancelacionTest {

    private static final Long ID_PEDIDO = 40L;
    private static final Long ID_CLIENTE = 7L;

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
                restaurantePersistencePort, trazabilidadClientPort, usuarioClientPort);
    }

    private Pedido pedido(Long idCliente, EstadoPedido estado) {
        Pedido pedido = new Pedido();
        pedido.setId(ID_PEDIDO);
        pedido.setIdCliente(idCliente);
        pedido.setIdRestaurante(3L);
        pedido.setEstado(estado);
        return pedido;
    }

    @Test
    void mensajeExactoDelEnunciado() {
        assertEquals("Lo sentimos, tu pedido ya est\u00e1 en preparaci\u00f3n y no puede cancelarse",
                MensajeError.PEDIDO_NO_CANCELABLE);
    }

    @Test
    void clienteDuenoConPedidoPendiente_cancelaYRegistraElLog() {
        when(pedidoPersistencePort.obtenerPedidoPorId(ID_PEDIDO))
                .thenReturn(Optional.of(pedido(ID_CLIENTE, EstadoPedido.PENDIENTE)));
        when(pedidoPersistencePort.guardarPedido(any(Pedido.class))).thenAnswer(i -> i.getArgument(0));

        Pedido resultado = pedidoUseCase.cancelarPedido(ID_PEDIDO, ID_CLIENTE);

        assertEquals(EstadoPedido.CANCELADO, resultado.getEstado());

        ArgumentCaptor<RegistroTrazabilidad> captor = ArgumentCaptor.forClass(RegistroTrazabilidad.class);
        verify(trazabilidadClientPort).registrarCambioEstado(captor.capture());
        RegistroTrazabilidad registro = captor.getValue();
        assertEquals(ID_PEDIDO, registro.idPedido());
        assertEquals(ID_CLIENTE, registro.idCliente());
        assertEquals(3L, registro.idRestaurante());
        assertNull(registro.idEmpleado());
        assertEquals(EstadoPedido.PENDIENTE, registro.estadoAnterior());
        assertEquals(EstadoPedido.CANCELADO, registro.estadoNuevo());
    }

    @Test
    void pedidoInexistente_lanzaNoEncontrado() {
        when(pedidoPersistencePort.obtenerPedidoPorId(ID_PEDIDO)).thenReturn(Optional.empty());

        assertThrows(PedidoNoEncontradoException.class,
                () -> pedidoUseCase.cancelarPedido(ID_PEDIDO, ID_CLIENTE));

        verify(pedidoPersistencePort, never()).guardarPedido(any());
        verifyNoInteractions(trazabilidadClientPort);
    }

    @Test
    void pedidoDeOtroCliente_lanzaNoAutorizadoSinGuardarNiRegistrar() {
        when(pedidoPersistencePort.obtenerPedidoPorId(ID_PEDIDO))
                .thenReturn(Optional.of(pedido(99L, EstadoPedido.PENDIENTE)));

        ClienteNoAutorizadoException e = assertThrows(ClienteNoAutorizadoException.class,
                () -> pedidoUseCase.cancelarPedido(ID_PEDIDO, ID_CLIENTE));

        assertEquals(MensajeError.PEDIDO_DE_OTRO_CLIENTE, e.getMessage());
        verify(pedidoPersistencePort, never()).guardarPedido(any());
        verifyNoInteractions(trazabilidadClientPort);
    }

    @Test
    void clienteNulo_lanzaNoAutorizado() {
        when(pedidoPersistencePort.obtenerPedidoPorId(ID_PEDIDO))
                .thenReturn(Optional.of(pedido(ID_CLIENTE, EstadoPedido.PENDIENTE)));

        assertThrows(ClienteNoAutorizadoException.class,
                () -> pedidoUseCase.cancelarPedido(ID_PEDIDO, null));
    }

    @Test
    void pedidoQueNoEstaPendiente_lanzaElMensajeDelEnunciadoSinGuardarNiRegistrar() {
        for (EstadoPedido estado : new EstadoPedido[]{EstadoPedido.EN_PREPARACION, EstadoPedido.LISTO,
                EstadoPedido.ENTREGADO, EstadoPedido.CANCELADO}) {
            when(pedidoPersistencePort.obtenerPedidoPorId(ID_PEDIDO))
                    .thenReturn(Optional.of(pedido(ID_CLIENTE, estado)));

            DomainException e = assertThrows(DomainException.class,
                    () -> pedidoUseCase.cancelarPedido(ID_PEDIDO, ID_CLIENTE));

            assertEquals(MensajeError.PEDIDO_NO_CANCELABLE, e.getMessage());
        }
        verify(pedidoPersistencePort, never()).guardarPedido(any());
        verifyNoInteractions(trazabilidadClientPort);
    }
}