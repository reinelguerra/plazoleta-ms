package com.plazoleta.plazoleta.application.usecase;

import com.plazoleta.plazoleta.domain.exception.DomainException;
import com.plazoleta.plazoleta.domain.exception.PedidoNoEncontradoException;
import com.plazoleta.plazoleta.domain.exception.UsuarioNoEncontradoException;
import com.plazoleta.plazoleta.domain.model.DatosUsuario;
import com.plazoleta.plazoleta.domain.model.EstadoPedido;
import com.plazoleta.plazoleta.domain.model.Pedido;
import com.plazoleta.plazoleta.domain.model.RegistroTrazabilidad;
import com.plazoleta.plazoleta.domain.model.RolUsuario;
import com.plazoleta.plazoleta.domain.spi.GeneradorPinPort;
import com.plazoleta.plazoleta.domain.spi.MensajeriaClientPort;
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
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/** HU 14: el empleado marca EN_PREPARACION → LISTO, se genera el PIN y se avisa al cliente por SMS. */
@ExtendWith(MockitoExtension.class)
class PedidoUseCaseListoTest {

    private static final Long ID_PEDIDO = 40L;
    private static final Long ID_EMPLEADO = 12L;
    private static final Long ID_CLIENTE = 7L;
    private static final Long ID_RESTAURANTE = 3L;
    private static final String CELULAR_CLIENTE = "+573001234567";
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

    @Mock
    private MensajeriaClientPort mensajeriaClientPort;

    @Mock
    private GeneradorPinPort generadorPinPort;

    private PedidoUseCase pedidoUseCase;

    @BeforeEach
    void preparar() {
        pedidoUseCase = new PedidoUseCase(pedidoPersistencePort, platoPersistencePort,
                restaurantePersistencePort, trazabilidadClientPort, usuarioClientPort,
                mensajeriaClientPort, generadorPinPort);
    }

    private void empleadoConRestaurante(Long idRestaurante) {
        when(usuarioClientPort.obtenerUsuarioPorId(ID_EMPLEADO)).thenReturn(Optional.of(
                new DatosUsuario(ID_EMPLEADO, RolUsuario.EMPLEADO, "+573001112233", idRestaurante)));
    }

    private void clienteConCelular(String celular) {
        when(usuarioClientPort.obtenerUsuarioPorId(ID_CLIENTE)).thenReturn(Optional.of(
                new DatosUsuario(ID_CLIENTE, RolUsuario.CLIENTE, celular, null)));
    }

    private Pedido pedido(Long idRestaurante, EstadoPedido estado) {
        Pedido pedido = new Pedido();
        pedido.setId(ID_PEDIDO);
        pedido.setIdCliente(ID_CLIENTE);
        pedido.setIdRestaurante(idRestaurante);
        pedido.setIdEmpleado(ID_EMPLEADO);
        pedido.setEstado(estado);
        return pedido;
    }

    @Test
    void pedidoEnPreparacion_generaPinEnviaSmsAntesDeGuardarYRegistraElLog() {
        empleadoConRestaurante(ID_RESTAURANTE);
        clienteConCelular(CELULAR_CLIENTE);
        when(pedidoPersistencePort.obtenerPedidoPorId(ID_PEDIDO))
                .thenReturn(Optional.of(pedido(ID_RESTAURANTE, EstadoPedido.EN_PREPARACION)));
        when(generadorPinPort.generar()).thenReturn(PIN);
        when(pedidoPersistencePort.guardarPedido(any(Pedido.class))).thenAnswer(i -> i.getArgument(0));

        Pedido resultado = pedidoUseCase.marcarPedidoListo(ID_PEDIDO, ID_EMPLEADO);

        assertEquals(EstadoPedido.LISTO, resultado.getEstado());
        assertEquals(PIN, resultado.getPin());
        assertEquals(ID_EMPLEADO, resultado.getIdEmpleado());

        InOrder orden = inOrder(mensajeriaClientPort, pedidoPersistencePort);
        orden.verify(mensajeriaClientPort).enviarSms(CELULAR_CLIENTE,
                "Tu pedido 40 esta listo para recoger. PIN de entrega: 123456");
        orden.verify(pedidoPersistencePort).guardarPedido(any(Pedido.class));

        ArgumentCaptor<RegistroTrazabilidad> captor = ArgumentCaptor.forClass(RegistroTrazabilidad.class);
        verify(trazabilidadClientPort).registrarCambioEstado(captor.capture());
        RegistroTrazabilidad registro = captor.getValue();
        assertEquals(ID_PEDIDO, registro.idPedido());
        assertEquals(ID_CLIENTE, registro.idCliente());
        assertEquals(ID_RESTAURANTE, registro.idRestaurante());
        assertEquals(ID_EMPLEADO, registro.idEmpleado());
        assertEquals(EstadoPedido.EN_PREPARACION, registro.estadoAnterior());
        assertEquals(EstadoPedido.LISTO, registro.estadoNuevo());
    }

    @Test
    void empleadoInexistente_lanzaNoEncontradoSinTocarNada() {
        when(usuarioClientPort.obtenerUsuarioPorId(ID_EMPLEADO)).thenReturn(Optional.empty());

        assertThrows(UsuarioNoEncontradoException.class,
                () -> pedidoUseCase.marcarPedidoListo(ID_PEDIDO, ID_EMPLEADO));

        verifyNoInteractions(pedidoPersistencePort, mensajeriaClientPort, trazabilidadClientPort,
                generadorPinPort);
    }

    @Test
    void empleadoSinRestaurante_lanzaExcepcionSinTocarNada() {
        empleadoConRestaurante(null);

        DomainException e = assertThrows(DomainException.class,
                () -> pedidoUseCase.marcarPedidoListo(ID_PEDIDO, ID_EMPLEADO));

        assertEquals(MensajeError.EMPLEADO_SIN_RESTAURANTE, e.getMessage());
        verifyNoInteractions(pedidoPersistencePort, mensajeriaClientPort, trazabilidadClientPort,
                generadorPinPort);
    }

    @Test
    void pedidoInexistente_lanzaNoEncontradoSinEnviarSms() {
        empleadoConRestaurante(ID_RESTAURANTE);
        when(pedidoPersistencePort.obtenerPedidoPorId(ID_PEDIDO)).thenReturn(Optional.empty());

        assertThrows(PedidoNoEncontradoException.class,
                () -> pedidoUseCase.marcarPedidoListo(ID_PEDIDO, ID_EMPLEADO));

        verify(pedidoPersistencePort, never()).guardarPedido(any());
        verifyNoInteractions(mensajeriaClientPort, trazabilidadClientPort, generadorPinPort);
    }

    @Test
    void pedidoDeOtroRestaurante_lanzaExcepcionSinEnviarSmsNiGuardar() {
        empleadoConRestaurante(ID_RESTAURANTE);
        when(pedidoPersistencePort.obtenerPedidoPorId(ID_PEDIDO))
                .thenReturn(Optional.of(pedido(99L, EstadoPedido.EN_PREPARACION)));

        DomainException e = assertThrows(DomainException.class,
                () -> pedidoUseCase.marcarPedidoListo(ID_PEDIDO, ID_EMPLEADO));

        assertEquals(MensajeError.PEDIDO_DE_OTRO_RESTAURANTE, e.getMessage());
        verify(pedidoPersistencePort, never()).guardarPedido(any());
        verifyNoInteractions(mensajeriaClientPort, trazabilidadClientPort, generadorPinPort);
    }

    @Test
    void pedidoQueNoEstaEnPreparacion_lanzaExcepcionSinEnviarSmsNiGuardar() {
        for (EstadoPedido estado : new EstadoPedido[]{EstadoPedido.PENDIENTE, EstadoPedido.LISTO,
                EstadoPedido.ENTREGADO, EstadoPedido.CANCELADO}) {
            empleadoConRestaurante(ID_RESTAURANTE);
            when(pedidoPersistencePort.obtenerPedidoPorId(ID_PEDIDO))
                    .thenReturn(Optional.of(pedido(ID_RESTAURANTE, estado)));

            DomainException e = assertThrows(DomainException.class,
                    () -> pedidoUseCase.marcarPedidoListo(ID_PEDIDO, ID_EMPLEADO));

            assertEquals(MensajeError.PEDIDO_NO_EN_PREPARACION, e.getMessage());
        }
        verify(pedidoPersistencePort, never()).guardarPedido(any());
        verifyNoInteractions(mensajeriaClientPort, trazabilidadClientPort, generadorPinPort);
    }

    @Test
    void clienteInexistente_lanzaNoEncontradoSinEnviarSmsNiGuardar() {
        empleadoConRestaurante(ID_RESTAURANTE);
        when(pedidoPersistencePort.obtenerPedidoPorId(ID_PEDIDO))
                .thenReturn(Optional.of(pedido(ID_RESTAURANTE, EstadoPedido.EN_PREPARACION)));
        when(usuarioClientPort.obtenerUsuarioPorId(ID_CLIENTE)).thenReturn(Optional.empty());

        assertThrows(UsuarioNoEncontradoException.class,
                () -> pedidoUseCase.marcarPedidoListo(ID_PEDIDO, ID_EMPLEADO));

        verify(pedidoPersistencePort, never()).guardarPedido(any());
        verifyNoInteractions(mensajeriaClientPort, trazabilidadClientPort, generadorPinPort);
    }

    @Test
    void clienteSinCelular_lanzaExcepcionSinEnviarSmsNiGuardar() {
        empleadoConRestaurante(ID_RESTAURANTE);
        clienteConCelular("  ");
        when(pedidoPersistencePort.obtenerPedidoPorId(ID_PEDIDO))
                .thenReturn(Optional.of(pedido(ID_RESTAURANTE, EstadoPedido.EN_PREPARACION)));

        DomainException e = assertThrows(DomainException.class,
                () -> pedidoUseCase.marcarPedidoListo(ID_PEDIDO, ID_EMPLEADO));

        assertEquals(MensajeError.CLIENTE_SIN_CELULAR, e.getMessage());
        verify(pedidoPersistencePort, never()).guardarPedido(any());
        verifyNoInteractions(mensajeriaClientPort, trazabilidadClientPort, generadorPinPort);
    }

    @Test
    void siFallaElSms_elPedidoNoSeGuardaNiSeRegistraElLog() {
        empleadoConRestaurante(ID_RESTAURANTE);
        clienteConCelular(CELULAR_CLIENTE);
        when(pedidoPersistencePort.obtenerPedidoPorId(ID_PEDIDO))
                .thenReturn(Optional.of(pedido(ID_RESTAURANTE, EstadoPedido.EN_PREPARACION)));
        when(generadorPinPort.generar()).thenReturn(PIN);
        doThrow(new IllegalStateException("mensajeria caida"))
                .when(mensajeriaClientPort).enviarSms(anyString(), anyString());

        assertThrows(IllegalStateException.class,
                () -> pedidoUseCase.marcarPedidoListo(ID_PEDIDO, ID_EMPLEADO));

        verify(pedidoPersistencePort, never()).guardarPedido(any());
        verifyNoInteractions(trazabilidadClientPort);
    }
}