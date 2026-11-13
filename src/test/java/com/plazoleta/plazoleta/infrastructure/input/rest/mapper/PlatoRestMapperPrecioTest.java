package com.plazoleta.plazoleta.infrastructure.input.rest.mapper;

import com.plazoleta.plazoleta.domain.exception.DomainException;
import com.plazoleta.plazoleta.domain.util.MensajeError;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PlatoRestMapperPrecioTest {

    private final PlatoRestMapper mapper = Mappers.getMapper(PlatoRestMapper.class);

    @Test
    void precioEntero_convierteBigDecimalSinDecimales() {
        assertEquals(25000, mapper.precioEntero(new BigDecimal("25000")));
        assertEquals(10, mapper.precioEntero(new BigDecimal("10.00")));
    }

    @Test
    void precioNulo_devuelveNulo() {
        assertNull(mapper.precioEntero(null));
    }

    @Test
    void precioConDecimales_lanzaElMensajeCentralizado() {
        DomainException e = assertThrows(DomainException.class,
                () -> mapper.precioEntero(new BigDecimal("10.5")));
        assertEquals(MensajeError.PRECIO_PLATO_NO_ENTERO, e.getMessage());
    }
}