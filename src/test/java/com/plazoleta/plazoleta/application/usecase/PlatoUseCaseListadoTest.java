package com.plazoleta.plazoleta.application.usecase;

import com.plazoleta.plazoleta.domain.exception.DomainException;
import com.plazoleta.plazoleta.domain.exception.RestauranteNoEncontradoException;
import com.plazoleta.plazoleta.domain.model.PaginaRespuesta;
import com.plazoleta.plazoleta.domain.model.Plato;
import com.plazoleta.plazoleta.domain.model.Restaurante;
import com.plazoleta.plazoleta.domain.spi.PlatoPersistencePort;
import com.plazoleta.plazoleta.domain.spi.RestaurantePersistencePort;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlatoUseCaseListadoTest {

    @Mock
    private PlatoPersistencePort platoPersistencePort;

    @Mock
    private RestaurantePersistencePort restaurantePersistencePort;

    private PlatoUseCase platoUseCase;

    @BeforeEach
    void preparar() {
        platoUseCase = new PlatoUseCase(platoPersistencePort, restaurantePersistencePort);
    }

    private Restaurante restaurante() {
        return new Restaurante(1L, "Sabor 24", "900123456", "Calle 10 # 5-20",
                "+573001234567", "https://logo.com/s.png", 10L);
    }

    private void restauranteExiste() {
        when(restaurantePersistencePort.obtenerRestaurantePorId(1L)).thenReturn(Optional.of(restaurante()));
    }

    @Test
    void sinCategoria_consultaLosPlatosActivosSinFiltro() {
        PaginaRespuesta<Plato> esperada = new PaginaRespuesta<>(List.of(), 0, 10, 0, 0);
        restauranteExiste();
        when(platoPersistencePort.listarPlatosActivosPorRestaurante(1L, null, 0, 10)).thenReturn(esperada);

        PaginaRespuesta<Plato> resultado = platoUseCase.listarPlatos(1L, null, 0, 10);

        assertSame(esperada, resultado);
    }

    @Test
    void categoriaEnBlanco_seTrataComoSinFiltro() {
        restauranteExiste();
        when(platoPersistencePort.listarPlatosActivosPorRestaurante(1L, null, 0, 10))
                .thenReturn(new PaginaRespuesta<>(List.of(), 0, 10, 0, 0));

        platoUseCase.listarPlatos(1L, "   ", 0, 10);

        verify(platoPersistencePort).listarPlatosActivosPorRestaurante(1L, null, 0, 10);
    }

    @Test
    void categoriaConEspacios_seEnviaSinEspacios() {
        restauranteExiste();
        when(platoPersistencePort.listarPlatosActivosPorRestaurante(1L, "Postre", 2, 5))
                .thenReturn(new PaginaRespuesta<>(List.of(), 2, 5, 0, 0));

        platoUseCase.listarPlatos(1L, "  Postre ", 2, 5);

        verify(platoPersistencePort).listarPlatosActivosPorRestaurante(1L, "Postre", 2, 5);
    }

    @Test
    void restauranteInexistente_lanzaNoEncontradoSinListarPlatos() {
        when(restaurantePersistencePort.obtenerRestaurantePorId(99L)).thenReturn(Optional.empty());

        assertThrows(RestauranteNoEncontradoException.class,
                () -> platoUseCase.listarPlatos(99L, null, 0, 10));

        verifyNoInteractions(platoPersistencePort);
    }

    @Test
    void paginaNegativa_lanzaExcepcionSinConsultar() {
        DomainException e = assertThrows(DomainException.class,
                () -> platoUseCase.listarPlatos(1L, null, -1, 10));

        assertEquals(MensajeError.PAGINA_NEGATIVA, e.getMessage());
        verifyNoInteractions(restaurantePersistencePort, platoPersistencePort);
    }

    @Test
    void tamanioMenorQueUno_lanzaExcepcionSinConsultar() {
        DomainException e = assertThrows(DomainException.class,
                () -> platoUseCase.listarPlatos(1L, null, 0, 0));

        assertEquals(MensajeError.TAMANIO_PAGINA_INVALIDO, e.getMessage());
        verifyNoInteractions(restaurantePersistencePort, platoPersistencePort);
    }

    @Test
    void restauranteNulo_lanzaExcepcionSinConsultar() {
        DomainException e = assertThrows(DomainException.class,
                () -> platoUseCase.listarPlatos(null, null, 0, 10));

        assertEquals(MensajeError.RESTAURANTE_OBLIGATORIO, e.getMessage());
        verifyNoInteractions(restaurantePersistencePort, platoPersistencePort);
    }
}