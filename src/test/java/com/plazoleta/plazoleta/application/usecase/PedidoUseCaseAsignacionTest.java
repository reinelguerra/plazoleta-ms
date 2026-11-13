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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/** HU 13: el empleado toma un pedido PENDIENTE de su restaurante y lo pasa a EN_PREPARACION. */
@ExtendWith(MockitoExtension.class)
class PedidoUseCaseAsignacionTest {

    private static final Long ID_PEDIDO = 40L;
    private static final Long ID_EMPLEADO = 12L;
    private static final Long ID_RESTAURANTE = 3L;

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

    private void empleadoConRestaurante(Long idRestaurante) {
        when(usuarioClientPort.obtenerUsuarioPorId(ID_EMPLEADO)).thenReturn(Optional.of(
                new DatosUsuario(ID_EMPLEADO, RolUsuario.EMPLEADO, "+573001112233", idRestaurante)));
    }

    private Pedido pedido(Long idRestaurante, EstadoPedido estado) {
        Pedido pedido = new Pedido();
        pedido.setId(ID_PEDIDO);
        pedido.setIdCliente(7L);
        pedido.setIdRestaurante(idRestaurante);
        pedido.setEstado(estado);
        return pedido;
    }

    @Test
    void pedidoPendienteDelRestaurante_asignaEmpleado_pasaAEnPreparacionYRegistraElLog() {
        empleadoConRestaurante(ID_RESTAURANTE);
        when(pedidoPersistencePort.obtenerPedidoPorId(ID_PEDIDO))
                .thenReturn(Optional.of(pedido(ID_RESTAURANTE, EstadoPedido.PENDIENTE)));
        when(pedidoPersistencePort.guardarPedido(any(Pedido.class))).thenAnswer(i -> i.getArgument(0));

        Pedido resultado = pedidoUseCase.iniciarPreparacion(ID_PEDIDO, ID_EMPLEADO);

        assertEquals(EstadoPedido.EN_PREPARACION, resultado.getEstado());
        assertEquals(ID_EMPLEADO, resultado.getIdEmpleado());
        assertNull(resultado.getPin());

        ArgumentCaptor<RegistroTrazabilidad> captor = ArgumentCaptor.forClass(RegistroTrazabilidad.class);
        verify(trazabilidadClientPort).registrarCambioEstado(captor.capture());
        RegistroTrazabilidad registro = captor.getValue();
        assertEquals(ID_PEDIDO, registro.idPedido());
        assertEquals(7L, registro.idCliente());
        assertEquals(ID_RESTAURANTE, registro.idRestaurante());
        assertEquals(ID_EMPLEADO, registro.idEmpleado());
        assertEquals(EstadoPedido.PENDIENTE, registro.estadoAnterior());
        assertEquals(EstadoPedido.EN_PREPARACION, registro.estadoNuevo());
    }

    @Test
    void empleadoInexistente_lanzaNoEncontradoSinTocarElPedido() {
        when(usuarioClientPort.obtenerUsuarioPorId(ID_EMPLEADO)).thenReturn(Optional.empty());

        assertThrows(UsuarioNoEncontradoException.class,
                () -> pedidoUseCase.iniciarPreparacion(ID_PEDIDO, ID_EMPLEADO));

        verifyNoInteractions(pedidoPersistencePort, trazabilidadClientPort);
    }

    @Test
    void empleadoSinRestaurante_lanzaExcepcionSinTocarElPedido() {
        empleadoConRestaurante(null);

        DomainException e = assertThrows(DomainException.class,
                () -> pedidoUseCase.iniciarPreparacion(ID_PEDIDO, ID_EMPLEADO));

        assertEquals(MensajeError.EMPLEADO_SIN_RESTAURANTE, e.getMessage());
        verifyNoInteractions(pedidoPersistencePort, trazabilidadClientPort);
    }

    @Test
    void pedidoInexistente_lanzaNoEncontrado() {
        empleadoConRestaurante(ID_RESTAURANTE);
        when(pedidoPersistencePort.obtenerPedidoPorId(ID_PEDIDO)).thenReturn(Optional.empty());

        assertThrows(PedidoNoEncontradoException.class,
                () -> pedidoUseCase.iniciarPreparacion(ID_PEDIDO, ID_EMPLEADO));

        verify(pedidoPersistencePort, never()).guardarPedido(any());
        verifyNoInteractions(trazabilidadClientPort);
    }

    @Test
    void pedidoDeOtroRestaurante_lanzaExcepcionSinGuardarNiRegistrar() {
        empleadoConRestaurante(ID_RESTAURANTE);
        when(pedidoPersistencePort.obtenerPedidoPorId(ID_PEDIDO))
                .thenReturn(Optional.of(pedido(99L, EstadoPedido.PENDIENTE)));

        DomainException e = assertThrows(DomainException.class,
                () -> pedidoUseCase.iniciarPreparacion(ID_PEDIDO, ID_EMPLEADO));

        assertEquals(MensajeError.PEDIDO_DE_OTRO_RESTAURANTE, e.getMessage());
        verify(pedidoPersistencePort, never()).guardarPedido(any());
        verifyNoInteractions(trazabilidadClientPort);
    }

    @Test
    void pedidoQueNoEstaPendiente_lanzaExcepcionSinGuardarNiRegistrar() {
        for (EstadoPedido estado : new EstadoPedido[]{EstadoPedido.EN_PREPARACION, EstadoPedido.LISTO,
                EstadoPedido.ENTREGADO, EstadoPedido.CANCELADO}) {
            empleadoConRestaurante(ID_RESTAURANTE);
            when(pedidoPersistencePort.obtenerPedidoPorId(ID_PEDIDO))
                    .thenReturn(Optional.of(pedido(ID_RESTAURANTE, estado)));

            DomainException e = assertThrows(DomainException.class,
                    () -> pedidoUseCase.iniciarPreparacion(ID_PEDIDO, ID_EMPLEADO));

            assertEquals(MensajeError.PEDIDO_NO_PENDIENTE, e.getMessage());
        }
        verify(pedidoPersistencePort, never()).guardarPedido(any());
        verifyNoInteractions(trazabilidadClientPort);
    }
}