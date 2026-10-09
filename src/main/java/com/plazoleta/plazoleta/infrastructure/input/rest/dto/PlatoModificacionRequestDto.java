package com.plazoleta.plazoleta.infrastructure.input.rest.dto;

import java.math.BigDecimal;

/**
 * HU 4: solo se pueden modificar el precio y la descripción.
 * El precio llega como BigDecimal (igual que al crear) para poder rechazar decimales.
 */
public record PlatoModificacionRequestDto(BigDecimal precio, String descripcion) {
}