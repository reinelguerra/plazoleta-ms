package com.plazoleta.plazoleta.infrastructure.input.rest.dto;

import com.plazoleta.plazoleta.domain.util.MensajeError;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Schema(description = "Datos para crear un restaurante")
public record RestauranteRequest(
        @NotBlank(message = MensajeError.NOMBRE_RESTAURANTE_OBLIGATORIO)
        @Pattern(regexp = "^(?!\\s*\\d+\\s*$).+$", message = MensajeError.NOMBRE_RESTAURANTE_SOLO_NUMEROS)
        @Schema(description = "Nombre del restaurante. Puede incluir números, pero no estar formado solo por números",
                example = "Sabor 24", requiredMode = REQUIRED) String nombre,

        @NotBlank(message = MensajeError.NIT_OBLIGATORIO)
        @Pattern(regexp = "^\\d+$", message = MensajeError.NIT_NO_NUMERICO)
        @Schema(description = "NIT: solo dígitos", example = "900123456",
                pattern = "^\\d+$", requiredMode = REQUIRED) String nit,

        @NotBlank(message = MensajeError.DIRECCION_RESTAURANTE_OBLIGATORIA)
        @Schema(description = "Dirección del restaurante", example = "Calle 10 # 5-20",
                requiredMode = REQUIRED) String direccion,

        @NotBlank(message = MensajeError.TELEFONO_RESTAURANTE_OBLIGATORIO)
        @Pattern(regexp = "^\\+?\\d{1,12}$", message = MensajeError.TELEFONO_RESTAURANTE_INVALIDO)
        @Schema(description = "Teléfono: máximo 13 caracteres, solo dígitos y opcionalmente un + al inicio",
                example = "+573001234567", maxLength = 13, pattern = "^\\+?\\d{1,12}$",
                requiredMode = REQUIRED) String telefono,

        @NotBlank(message = MensajeError.URL_LOGO_OBLIGATORIA)
        @Schema(description = "URL del logo", example = "https://logo.com/sabor.png",
                requiredMode = REQUIRED) String urlLogo,

        @NotNull(message = MensajeError.PROPIETARIO_RESTAURANTE_OBLIGATORIO)
        @Schema(description = "Id del propietario: debe existir en usuarios-ms con rol PROPIETARIO",
                example = "2", requiredMode = REQUIRED) Long idPropietario) {
}