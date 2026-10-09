package com.plazoleta.plazoleta.application.usecase;

import com.plazoleta.plazoleta.domain.exception.DomainException;
import com.plazoleta.plazoleta.domain.model.Pagina;
import com.plazoleta.plazoleta.domain.model.Restaurante;
import com.plazoleta.plazoleta.domain.spi.RestaurantePersistencePort;
import com.plazoleta.plazoleta.domain.spi.UsuarioClientPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RestauranteUseCaseListadoTest {

    @Mock
    private RestaurantePersistencePort restaurantePersistencePort;

    @Mock
    private UsuarioClientPort usuarioClientPort;

    @InjectMocks
    private RestauranteUseCase restauranteUseCase;

    @Test
    void parametrosValidos_delegaEnElPuertoYDevuelveLaPagina() {
        Pagina<Restaurante> esperada = new Pagina<>(List.of(), 2, 5, 0, 0);
        when(restaurantePersistencePort.listarRestaurantes(2, 5)).thenReturn(esperada);

        Pagina<Restaurante> resultado = restauranteUseCase.listarRestaurantes(2, 5);

        assertSame(esperada, resultado);
        verify(restaurantePersistencePort).listarRestaurantes(2, 5);
    }

    @Test
    void paginaNegativa_lanzaExcepcionSinConsultar() {
        assertThrows(DomainException.class, () -> restauranteUseCase.listarRestaurantes(-1, 10));

        verifyNoInteractions(restaurantePersistencePort);
    }

    @Test
    void tamanioMenorQueUno_lanzaExcepcionSinConsultar() {
        assertThrows(DomainException.class, () -> restauranteUseCase.listarRestaurantes(0, 0));

        verifyNoInteractions(restaurantePersistencePort);
    }
}