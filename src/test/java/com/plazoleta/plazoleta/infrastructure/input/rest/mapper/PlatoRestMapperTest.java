package com.plazoleta.plazoleta.infrastructure.input.rest.mapper;

import com.plazoleta.plazoleta.domain.exception.DomainException;
import com.plazoleta.plazoleta.domain.model.Plato;
import com.plazoleta.plazoleta.domain.util.MensajeError;
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
    void toModel_copiaTodosLosCampos_yDejaIdYActivoSinAsignar() {
        Plato plato = mapper.toModel(request(new BigDecimal("15000")));

        assertEquals("Hamburguesa", plato.getNombre());
        assertEquals(15000, plato.getPrecio());
        assertEquals("Con queso", plato.getDescripcion());
        assertEquals("http://imagenes.com/h.png", plato.getUrlImagen());
        assertEquals("PLATO_FUERTE", plato.getCategoria());
        assertEquals(1L, plato.getIdRestaurante());
        assertNull(plato.getId());
        assertNull(plato.getActivo());
    }

    @Test
    void toModel_precioConDecimalesEnCero_loConvierte() {
        Plato plato = mapper.toModel(request(new BigDecimal("15000.0")));
        assertEquals(15000, plato.getPrecio());
    }

    @Test
    void toModel_precioConDecimales_lanzaElMensajeCentralizado() {
        DomainException e = assertThrows(DomainException.class,
                () -> mapper.toModel(request(new BigDecimal("15.5"))));
        assertEquals(MensajeError.PRECIO_PLATO_NO_ENTERO, e.getMessage());
    }

    @Test
    void toModel_sinPrecio_dejaPrecioVacio() {
        Plato plato = mapper.toModel(request(null));
        assertNull(plato.getPrecio());
    }
}