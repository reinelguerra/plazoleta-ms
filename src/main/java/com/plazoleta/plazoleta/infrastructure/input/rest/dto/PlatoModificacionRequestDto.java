package com.plazoleta.plazoleta.infrastructure.input.rest.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.plazoleta.plazoleta.domain.util.MensajeError;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

/** El precio es BigDecimal en el request por la misma razón que en PlatoRequestDto. */
@Schema(description = "Datos que se pueden modificar de un plato: solo precio y descripción")
public record PlatoModificacionRequestDto(
        @NotNull(message = MensajeError.PRECIO_PLATO_INVALIDO)
        @Positive(message = MensajeError.PRECIO_PLATO_INVALIDO)
        @Schema(description = "Nuevo precio en pesos: número entero mayor a 0", type = "integer",
                example = "27000", minimum = "1", requiredMode = REQUIRED) BigDecimal precio,

        @NotBlank(message = MensajeError.DESCRIPCION_PLATO_OBLIGATORIA)
        @Schema(description = "Nueva descripción del plato", example = "Ahora con huevo y aguacate",
                requiredMode = REQUIRED) String descripcion) {

    @JsonIgnore
    @Schema(hidden = true)
    @AssertTrue(message = MensajeError.PRECIO_PLATO_NO_ENTERO)
    public boolean isPrecioEntero() {
        return precio == null
                || (precio.stripTrailingZeros().scale() <= 0
                && precio.compareTo(BigDecimal.valueOf(Integer.MAX_VALUE)) <= 0);
    }
}