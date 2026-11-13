package com.plazoleta.plazoleta.application.usecase;

import com.plazoleta.plazoleta.domain.exception.DomainException;
import com.plazoleta.plazoleta.domain.model.EstadoPedido;
import com.plazoleta.plazoleta.domain.model.Pedido;
import com.plazoleta.plazoleta.domain.model.PedidoPlato;
import com.plazoleta.plazoleta.domain.model.Plato;
import com.plazoleta.plazoleta.domain.model.RegistroTrazabilidad;
import com.plazoleta.plazoleta.domain.model.Restaurante;
import com.plazoleta.plazoleta.domain.spi.PedidoPersistencePort;
import com.plazoleta.plazoleta.domain.spi.PlatoPersistencePort;
import com.plazoleta.plazoleta.domain.spi.RestaurantePersistencePort;
import com.plazoleta.plazoleta.domain.spi.TrazabilidadClientPort;
import com.plazoleta.plazoleta.domain.spi.UsuarioClientPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/** HU 11: al crear el pedido se registra el log inicial en estado PENDIENTE. */
@ExtendWith(MockitoExtension.class)
class PedidoUseCaseTrazabilidadTest {

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

    private Pedido pedido() {
        Pedido pedido = new Pedido();
        pedido.setIdCliente(7L);
        pedido.setIdRestaurante(1L);
        pedido.setPlatos(new ArrayList<>(List.of(new PedidoPlato(10L, 2))));
        return pedido;
    }

    private void restauranteExiste() {
        when(restaurantePersistencePort.obtenerRestaurantePorId(1L)).thenReturn(Optional.of(
                new Restaurante(1L, "Sabor 24", "900123456", "Calle 10 # 5-20",
                        "+573001234567", "https://logo.com/s.png", 10L)));
    }

    private void todoValido() {
        restauranteExiste();
        when(pedidoPersistencePort.existePedidoDeClienteEnEstados(7L, EstadoPedido.EN_PROCESO)).thenReturn(false);
        when(platoPersistencePort.obtenerPlatoPorId(10L)).thenReturn(Optional.of(
                new Plato(10L, "Bandeja", 25000, "Descripcion", "https://img.com/b.png", "Fuerte", true, 1L)));
        when(pedidoPersistencePort.guardarPedido(any(Pedido.class))).thenAnswer(invocacion -> {
            Pedido guardado = invocacion.getArgument(0);
            guardado.setId(50L);
            return guardado;
        });
    }

    @Test
    void alCrearElPedido_registraElLogInicialPendienteSinEstadoAnteriorNiEmpleado() {
        todoValido();

        pedidoUseCase.realizarPedido(pedido());

        ArgumentCaptor<RegistroTrazabilidad> captor = ArgumentCaptor.forClass(RegistroTrazabilidad.class);
        verify(trazabilidadClientPort).registrarCambioEstado(captor.capture());
        RegistroTrazabilidad registro = captor.getValue();
        assertEquals(50L, registro.idPedido());
        assertEquals(7L, registro.idCliente());
        assertEquals(1L, registro.idRestaurante());
        assertNull(registro.idEmpleado());
        assertNull(registro.estadoAnterior());
        assertEquals(EstadoPedido.PENDIENTE, registro.estadoNuevo());
    }

    @Test
    void elLogSeRegistraDespuesDeGuardarElPedido() {
        todoValido();

        pedidoUseCase.realizarPedido(pedido());

        InOrder orden = inOrder(pedidoPersistencePort, trazabilidadClientPort);
        orden.verify(pedidoPersistencePort).guardarPedido(any(Pedido.class));
        orden.verify(trazabilidadClientPort).registrarCambioEstado(any(RegistroTrazabilidad.class));
    }

    @Test
    void siElPedidoNoSeCrea_noSeRegistraNingunLog() {
        restauranteExiste();
        when(pedidoPersistencePort.existePedidoDeClienteEnEstados(7L, EstadoPedido.EN_PROCESO)).thenReturn(true);

        assertThrows(DomainException.class, () -> pedidoUseCase.realizarPedido(pedido()));

        verifyNoInteractions(trazabilidadClientPort);
    }

    @Test
    void siTrazabilidadFalla_laExcepcionLlegaAlLlamador() {
        todoValido();
        doThrow(new IllegalStateException("trazabilidad caida"))
                .when(trazabilidadClientPort).registrarCambioEstado(any(RegistroTrazabilidad.class));

        assertThrows(IllegalStateException.class, () -> pedidoUseCase.realizarPedido(pedido()));
    }
}