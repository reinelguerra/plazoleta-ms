package com.plazoleta.plazoleta.application.usecase;

import com.plazoleta.plazoleta.domain.exception.DomainException;
import com.plazoleta.plazoleta.domain.exception.UsuarioNoEncontradoException;
import com.plazoleta.plazoleta.domain.model.DatosUsuario;
import com.plazoleta.plazoleta.domain.model.EstadoPedido;
import com.plazoleta.plazoleta.domain.model.PaginaRespuesta;
import com.plazoleta.plazoleta.domain.model.Pedido;
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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/** HU 12: el empleado solo ve los pedidos de su restaurante, filtrados por estado. */
@ExtendWith(MockitoExtension.class)
class PedidoUseCaseListadoTest {

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

    private void empleadoConRestaurante() {
        when(usuarioClientPort.obtenerUsuarioPorId(ID_EMPLEADO)).thenReturn(Optional.of(
                new DatosUsuario(ID_EMPLEADO, RolUsuario.EMPLEADO, "+573001112233", ID_RESTAURANTE)));
    }

    @Test
    void empleadoConRestaurante_listaLosPedidosDeSuRestauranteYDelEstadoPedido() {
        PaginaRespuesta<Pedido> esperada = new PaginaRespuesta<>(List.of(new Pedido()), 0, 10, 1, 1);
        empleadoConRestaurante();
        when(pedidoPersistencePort.listarPedidosPorRestauranteYEstado(
                ID_RESTAURANTE, EstadoPedido.PENDIENTE, 0, 10)).thenReturn(esperada);

        PaginaRespuesta<Pedido> resultado = pedidoUseCase.listarPedidosPorEstado(
                ID_EMPLEADO, EstadoPedido.PENDIENTE, 0, 10);

        assertSame(esperada, resultado);
    }

    @Test
    void empleadoSinRestaurante_lanzaExcepcionSinListar() {
        when(usuarioClientPort.obtenerUsuarioPorId(ID_EMPLEADO)).thenReturn(Optional.of(
                new DatosUsuario(ID_EMPLEADO, RolUsuario.EMPLEADO, "+573001112233", null)));

        DomainException e = assertThrows(DomainException.class,
                () -> pedidoUseCase.listarPedidosPorEstado(ID_EMPLEADO, EstadoPedido.PENDIENTE, 0, 10));

        assertEquals(MensajeError.EMPLEADO_SIN_RESTAURANTE, e.getMessage());
        verifyNoInteractions(pedidoPersistencePort);
    }

    @Test
    void usuarioInexistente_lanzaNoEncontradoSinListar() {
        when(usuarioClientPort.obtenerUsuarioPorId(ID_EMPLEADO)).thenReturn(Optional.empty());

        assertThrows(UsuarioNoEncontradoException.class,
                () -> pedidoUseCase.listarPedidosPorEstado(ID_EMPLEADO, EstadoPedido.PENDIENTE, 0, 10));

        verifyNoInteractions(pedidoPersistencePort);
    }

    @Test
    void estadoNulo_lanzaExcepcionSinConsultarNada() {
        DomainException e = assertThrows(DomainException.class,
                () -> pedidoUseCase.listarPedidosPorEstado(ID_EMPLEADO, null, 0, 10));

        assertEquals(MensajeError.ESTADO_PEDIDO_OBLIGATORIO, e.getMessage());
        verifyNoInteractions(usuarioClientPort, pedidoPersistencePort);
    }

    @Test
    void paginaNegativa_lanzaExcepcionSinConsultarNada() {
        DomainException e = assertThrows(DomainException.class,
                () -> pedidoUseCase.listarPedidosPorEstado(ID_EMPLEADO, EstadoPedido.LISTO, -1, 10));

        assertEquals(MensajeError.PAGINA_NEGATIVA, e.getMessage());
        verifyNoInteractions(usuarioClientPort, pedidoPersistencePort);
    }

    @Test
    void tamanioMenorQueUno_lanzaExcepcionSinConsultarNada() {
        DomainException e = assertThrows(DomainException.class,
                () -> pedidoUseCase.listarPedidosPorEstado(ID_EMPLEADO, EstadoPedido.LISTO, 0, 0));

        assertEquals(MensajeError.TAMANIO_PAGINA_INVALIDO, e.getMessage());
        verifyNoInteractions(usuarioClientPort, pedidoPersistencePort);
    }
}