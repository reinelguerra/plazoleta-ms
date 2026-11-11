package com.plazoleta.plazoleta.domain.util;

/** Mensajes de error centralizados. Aquí se irán agregando los de cada HU. */
public final class MensajeError {

    // Paginación (HU 9, 10, 12)
    public static final String PAGINA_NEGATIVA = "La página no puede ser negativa";
    public static final String TAMANIO_PAGINA_INVALIDO = "El tamaño de página debe ser al menos 1";
    public static final String RESTAURANTE_OBLIGATORIO = "El restaurante es obligatorio";

    // Pedidos (HU 11)
    public static final String PEDIDO_OBLIGATORIO = "Los datos del pedido son obligatorios";
    public static final String CLIENTE_OBLIGATORIO = "El cliente del pedido es obligatorio";
    public static final String PEDIDO_SIN_PLATOS = "El pedido debe tener al menos un plato";
    public static final String PLATO_PEDIDO_INVALIDO = "Cada plato del pedido debe indicar su id";
    public static final String CANTIDAD_INVALIDA = "La cantidad de cada plato debe ser un número entero mayor a 0";
    public static final String PLATO_REPETIDO = "No se puede repetir un plato en el mismo pedido";
    public static final String PLATO_DE_OTRO_RESTAURANTE =
            "Todos los platos del pedido deben pertenecer al restaurante del pedido";
    public static final String PLATO_INACTIVO = "Uno de los platos del pedido no está disponible";
    public static final String CLIENTE_CON_PEDIDO_EN_PROCESO =
            "Ya tienes un pedido en proceso: no puedes solicitar otro hasta que termine";

    private MensajeError() {
    }
}