package com.plazoleta.plazoleta.domain.util;

/** Mensajes de error centralizados. Aquí se irán agregando los de cada HU. */
public final class MensajeError {

    public static final String PAGINA_NEGATIVA = "La página no puede ser negativa";
    public static final String TAMANIO_PAGINA_INVALIDO = "El tamaño de página debe ser al menos 1";
    public static final String RESTAURANTE_OBLIGATORIO = "El restaurante es obligatorio";

    private MensajeError() {
    }
}