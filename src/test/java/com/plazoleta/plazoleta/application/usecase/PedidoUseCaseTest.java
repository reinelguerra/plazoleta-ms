package com.plazoleta.plazoleta.application.usecase;

import com.plazoleta.plazoleta.domain.exception.DomainException;
import com.plazoleta.plazoleta.domain.exception.PlatoNoEncontradoException;
import com.plazoleta.plazoleta.domain.exception.RestauranteNoEncontradoException;
import com.plazoleta.plazoleta.domain.model.EstadoPedido;
import com.plazoleta.plazoleta.domain.model.Pedido;
import com.plazoleta.plazoleta.domain.model.PedidoPlato;
import com.plazoleta.plazoleta.domain.model.Plato;
import com.plazoleta.plazoleta.domain.model.Restaurante;
import com.plazoleta.plazoleta.domain.spi.PedidoPersistencePort;
import com.plazoleta.plazoleta.domain.spi.PlatoPersistencePort;
import com.plazoleta.plazoleta.domain.spi.RestaurantePersistencePort;
import com.plazoleta.plazoleta.domain.util.MensajeError;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PedidoUseCaseTest {

    private static final Long ID_CLIENTE = 7L;
    private static final Long ID_RESTAURANTE = 1L;

    @Mock
    private PedidoPersistencePort pedidoPersistencePort;

    @Mock
    private PlatoPersistencePort platoPersistencePort;

    @Mock
    private RestaurantePersistencePort restaurantePersistencePort;

    private PedidoUseCase pedidoUseCase;

    @BeforeEach
    void preparar() {
        pedidoUseCase = new PedidoUseCase(pedidoPersistencePort, platoPersistencePort, restaurantePersistencePort);
    }

    private Pedido pedido(PedidoPlato... platos) {
        Pedido pedido = new Pedido();
        pedido.setIdCliente(ID_CLIENTE);
        pedido.setIdRestaurante(ID_RESTAURANTE);
        pedido.setPlatos(new ArrayList<>(List.of(platos)));
        return pedido;
    }

    private Plato plato(Long id, Long idRestaurante, boolean activo) {
        return new Plato(id, "Plato " + id, 10000, "Descripcion", "https://img.com/x.png",
                "Fuerte", activo, idRestaurante);
    }

    private void restauranteExiste() {
        when(restaurantePersistencePort.obtenerRestaurantePorId(ID_RESTAURANTE)).thenReturn(Optional.of(
                new Restaurante(ID_RESTAURANTE, "Sabor 24", "900123456", "Calle 10 # 5-20",
                        "+573001234567", "https://logo.com/s.png", 10L)));
    }

    private void clienteSinPedidoEnProceso() {
        when(pedidoPersistencePort.existePedidoDeClienteEnEstados(ID_CLIENTE, EstadoPedido.EN_PROCESO))
                .thenReturn(false);
    }

    private void verificarQueNoSeConsultoNada() {
        verifyNoInteractions(pedidoPersistencePort, platoPersistencePort, restaurantePersistencePort);
    }

    // ---------- camino feliz ----------

    @Test
    void pedidoValido_quedaPendienteConFechaYSinEmpleadoNiPin() {
        restauranteExiste();
        clienteSinPedidoEnProceso();
        when(platoPersistencePort.obtenerPlatoPorId(10L)).thenReturn(Optional.of(plato(10L, ID_RESTAURANTE, true)));
        when(platoPersistencePort.obtenerPlatoPorId(11L)).thenReturn(Optional.of(plato(11L, ID_RESTAURANTE, true)));
        when(pedidoPersistencePort.guardarPedido(any(Pedido.class))).thenAnswer(i -> i.getArgument(0));

        Pedido pedido = pedido(new PedidoPlato(10L, 2), new PedidoPlato(11L, 1));
        pedido.setEstado(EstadoPedido.LISTO);
        pedido.setIdEmpleado(99L);
        pedido.setPin("123456");

        Pedido resultado = pedidoUseCase.realizarPedido(pedido);

        assertEquals(EstadoPedido.PENDIENTE, resultado.getEstado());
        assertNotNull(resultado.getFecha());
        assertNull(resultado.getIdEmpleado());
        assertNull(resultado.getPin());
        assertEquals(2, resultado.getPlatos().size());
        verify(pedidoPersistencePort).guardarPedido(pedido);
    }

    // ---------- reglas de negocio ----------

    @Test
    void restauranteInexistente_lanzaNoEncontradoYNoGuarda() {
        when(restaurantePersistencePort.obtenerRestaurantePorId(ID_RESTAURANTE)).thenReturn(Optional.empty());

        assertThrows(RestauranteNoEncontradoException.class,
                () -> pedidoUseCase.realizarPedido(pedido(new PedidoPlato(10L, 1))));

        verify(pedidoPersistencePort, never()).guardarPedido(any());
    }

    @Test
    void clienteConPedidoEnProceso_noPuedePedirOtro() {
        restauranteExiste();
        when(pedidoPersistencePort.existePedidoDeClienteEnEstados(ID_CLIENTE, EstadoPedido.EN_PROCESO))
                .thenReturn(true);

        DomainException e = assertThrows(DomainException.class,
                () -> pedidoUseCase.realizarPedido(pedido(new PedidoPlato(10L, 1))));

        assertEquals(MensajeError.CLIENTE_CON_PEDIDO_EN_PROCESO, e.getMessage());
        verify(pedidoPersistencePort, never()).guardarPedido(any());
        verifyNoInteractions(platoPersistencePort);
    }

    @Test
    void platoInexistente_lanzaNoEncontradoYNoGuarda() {
        restauranteExiste();
        clienteSinPedidoEnProceso();
        when(platoPersistencePort.obtenerPlatoPorId(10L)).thenReturn(Optional.empty());

        assertThrows(PlatoNoEncontradoException.class,
                () -> pedidoUseCase.realizarPedido(pedido(new PedidoPlato(10L, 1))));

        verify(pedidoPersistencePort, never()).guardarPedido(any());
    }

    @Test
    void platoDeOtroRestaurante_lanzaExcepcionYNoGuarda() {
        restauranteExiste();
        clienteSinPedidoEnProceso();
        when(platoPersistencePort.obtenerPlatoPorId(10L)).thenReturn(Optional.of(plato(10L, 2L, true)));

        DomainException e = assertThrows(DomainException.class,
                () -> pedidoUseCase.realizarPedido(pedido(new PedidoPlato(10L, 1))));

        assertEquals(MensajeError.PLATO_DE_OTRO_RESTAURANTE, e.getMessage());
        verify(pedidoPersistencePort, never()).guardarPedido(any());
    }

    @Test
    void platoInactivo_lanzaExcepcionYNoGuarda() {
        restauranteExiste();
        clienteSinPedidoEnProceso();
        when(platoPersistencePort.obtenerPlatoPorId(10L)).thenReturn(Optional.of(plato(10L, ID_RESTAURANTE, false)));

        DomainException e = assertThrows(DomainException.class,
                () -> pedidoUseCase.realizarPedido(pedido(new PedidoPlato(10L, 1))));

        assertEquals(MensajeError.PLATO_INACTIVO, e.getMessage());
        verify(pedidoPersistencePort, never()).guardarPedido(any());
    }

    // ---------- validación de los datos (no consulta nada) ----------

    @Test
    void pedidoNulo_lanzaExcepcion() {
        DomainException e = assertThrows(DomainException.class, () -> pedidoUseCase.realizarPedido(null));

        assertEquals(MensajeError.PEDIDO_OBLIGATORIO, e.getMessage());
        verificarQueNoSeConsultoNada();
    }

    @Test
    void sinCliente_lanzaExcepcion() {
        Pedido pedido = pedido(new PedidoPlato(10L, 1));
        pedido.setIdCliente(null);

        DomainException e = assertThrows(DomainException.class, () -> pedidoUseCase.realizarPedido(pedido));

        assertEquals(MensajeError.CLIENTE_OBLIGATORIO, e.getMessage());
        verificarQueNoSeConsultoNada();
    }

    @Test
    void sinRestaurante_lanzaExcepcion() {
        Pedido pedido = pedido(new PedidoPlato(10L, 1));
        pedido.setIdRestaurante(null);

        DomainException e = assertThrows(DomainException.class, () -> pedidoUseCase.realizarPedido(pedido));

        assertEquals(MensajeError.RESTAURANTE_OBLIGATORIO, e.getMessage());
        verificarQueNoSeConsultoNada();
    }

    @Test
    void sinPlatos_lanzaExcepcion() {
        DomainException e = assertThrows(DomainException.class, () -> pedidoUseCase.realizarPedido(pedido()));

        assertEquals(MensajeError.PEDIDO_SIN_PLATOS, e.getMessage());
        verificarQueNoSeConsultoNada();
    }

    @Test
    void listaDePlatosNula_lanzaExcepcion() {
        Pedido pedido = pedido();
        pedido.setPlatos(null);

        DomainException e = assertThrows(DomainException.class, () -> pedidoUseCase.realizarPedido(pedido));

        assertEquals(MensajeError.PEDIDO_SIN_PLATOS, e.getMessage());
        verificarQueNoSeConsultoNada();
    }

    @Test
    void platoSinId_lanzaExcepcion() {
        DomainException e = assertThrows(DomainException.class,
                () -> pedidoUseCase.realizarPedido(pedido(new PedidoPlato(null, 1))));

        assertEquals(MensajeError.PLATO_PEDIDO_INVALIDO, e.getMessage());
        verificarQueNoSeConsultoNada();
    }

    @Test
    void cantidadCero_lanzaExcepcion() {
        DomainException e = assertThrows(DomainException.class,
                () -> pedidoUseCase.realizarPedido(pedido(new PedidoPlato(10L, 0))));

        assertEquals(MensajeError.CANTIDAD_INVALIDA, e.getMessage());
        verificarQueNoSeConsultoNada();
    }

    @Test
    void cantidadNegativa_lanzaExcepcion() {
        DomainException e = assertThrows(DomainException.class,
                () -> pedidoUseCase.realizarPedido(pedido(new PedidoPlato(10L, -2))));

        assertEquals(MensajeError.CANTIDAD_INVALIDA, e.getMessage());
        verificarQueNoSeConsultoNada();
    }

    @Test
    void cantidadNula_lanzaExcepcion() {
        DomainException e = assertThrows(DomainException.class,
                () -> pedidoUseCase.realizarPedido(pedido(new PedidoPlato(10L, null))));

        assertEquals(MensajeError.CANTIDAD_INVALIDA, e.getMessage());
        verificarQueNoSeConsultoNada();
    }

    @Test
    void platoRepetido_lanzaExcepcion() {
        DomainException e = assertThrows(DomainException.class,
                () -> pedidoUseCase.realizarPedido(pedido(new PedidoPlato(10L, 1), new PedidoPlato(10L, 2))));

        assertEquals(MensajeError.PLATO_REPETIDO, e.getMessage());
        verificarQueNoSeConsultoNada();
    }
}