package com.plazoleta.plazoleta.infrastructure.input.rest.dto;

import com.plazoleta.plazoleta.domain.util.MensajeError;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(description = "PIN de entrega que el cliente recibió por SMS")
public record PedidoEntregaRequestDto(
        @Schema(description = "PIN de 6 dígitos", example = "123456")
        @NotBlank(message = MensajeError.PIN_OBLIGATORIO)
        @Pattern(regexp = "^\\d{6}$", message = MensajeError.PIN_OBLIGATORIO)
        String pin) {
}