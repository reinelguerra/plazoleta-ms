package com.plazoleta.plazoleta.infrastructure.input.rest.mapper;

import com.plazoleta.plazoleta.domain.exception.DomainException;
import com.plazoleta.plazoleta.domain.model.Plato;
import com.plazoleta.plazoleta.infrastructure.input.rest.dto.PlatoRequestDto;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PlatoRestMapperTest {

    private final PlatoRestMapper mapper = Mappers.getMapper(PlatoRestMapper.class);

    private PlatoRequestDto request(BigDecimal precio) {
        return new PlatoRequestDto("Hamburguesa", precio, "Con queso",
                "http://imagenes.com/h.png", "PLATO_FUERTE", 1L);
    }

    @Test
    void toModel_precioEntero_loConvierte() {
        Plato plato = mapper.toModel(request(new BigDecimal("15000")));
        assertEquals(15000, plato.getPrecio());
    }

    @Test
    void toModel_precioConDecimalesEnCero_loConvierte() {
        Plato plato = mapper.toModel(request(new BigDecimal("15000.0")));
        assertEquals(15000, plato.getPrecio());
    }

    @Test
    void toModel_precioConDecimales_lanzaDomainException() {
        DomainException excepcion = assertThrows(DomainException.class,
                () -> mapper.toModel(request(new BigDecimal("15.5"))));
        assertEquals("El precio debe ser un número entero mayor a 0", excepcion.getMessage());
    }

    @Test
    void toModel_sinPrecio_dejaPrecioVacio() {
        Plato plato = mapper.toModel(request(null));
        assertNull(plato.getPrecio());
    }
}