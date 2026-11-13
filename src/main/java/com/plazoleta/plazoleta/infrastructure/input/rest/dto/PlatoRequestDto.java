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

/**
 * Decisión sobre el precio: en el dominio, la entidad y las respuestas es un Integer (pesos enteros). En el
 * request es un BigDecimal a propósito: Jackson convierte 10.5 en 10 en silencio cuando el campo es Integer,
 * y así un precio con decimales se aceptaría mal. Con BigDecimal el valor llega tal cual, se valida que sea
 * entero y mayor a 0, y el mapper lo pasa a Integer sin pérdida.
 */
@Schema(description = "Datos para crear un plato")
public record PlatoRequestDto(
        @NotBlank(message = MensajeError.NOMBRE_PLATO_OBLIGATORIO)
        @Schema(description = "Nombre del plato", example = "Bandeja paisa",
                requiredMode = REQUIRED) String nombre,

        @NotNull(message = MensajeError.PRECIO_PLATO_INVALIDO)
        @Positive(message = MensajeError.PRECIO_PLATO_INVALIDO)
        @Schema(description = "Precio en pesos: número entero mayor a 0", type = "integer", example = "25000",
                minimum = "1", requiredMode = REQUIRED) BigDecimal precio,

        @NotBlank(message = MensajeError.DESCRIPCION_PLATO_OBLIGATORIA)
        @Schema(description = "Descripción del plato", example = "Frijoles, arroz, carne molida y chicharrón",
                requiredMode = REQUIRED) String descripcion,

        @NotBlank(message = MensajeError.URL_IMAGEN_PLATO_OBLIGATORIA)
        @Schema(description = "URL de la imagen del plato", example = "https://imagenes.com/bandeja.png",
                requiredMode = REQUIRED) String urlImagen,

        @NotBlank(message = MensajeError.CATEGORIA_PLATO_OBLIGATORIA)
        @Schema(description = "Categoría del plato", example = "Plato fuerte",
                requiredMode = REQUIRED) String categoria,

        @NotNull(message = MensajeError.RESTAURANTE_PLATO_OBLIGATORIO)
        @Schema(description = "Id del restaurante al que pertenece: debe ser del propietario autenticado",
                example = "1", requiredMode = REQUIRED) Long idRestaurante) {

    /** Un precio sin decimales (15000 o 15000.0) y que quepa en un Integer. */
    @JsonIgnore
    @Schema(hidden = true)
    @AssertTrue(message = MensajeError.PRECIO_PLATO_NO_ENTERO)
    public boolean isPrecioEntero() {
        return precio == null
                || (precio.stripTrailingZeros().scale() <= 0
                && precio.compareTo(BigDecimal.valueOf(Integer.MAX_VALUE)) <= 0);
    }
}