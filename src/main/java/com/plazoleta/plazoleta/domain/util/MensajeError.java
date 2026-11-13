package com.plazoleta.plazoleta.domain.util;

/** Mensajes de error centralizados. Aquí se irán agregando los de cada HU. */
public final class MensajeError {

    // Restaurantes (HU 2)
    public static final String NOMBRE_RESTAURANTE_OBLIGATORIO = "El nombre del restaurante es obligatorio";
    public static final String NOMBRE_RESTAURANTE_SOLO_NUMEROS =
            "El nombre del restaurante no puede contener solo números";
    public static final String NIT_OBLIGATORIO = "El NIT es obligatorio";
    public static final String NIT_NO_NUMERICO = "El NIT debe ser numérico";
    public static final String DIRECCION_RESTAURANTE_OBLIGATORIA = "La dirección es obligatoria";
    public static final String TELEFONO_RESTAURANTE_OBLIGATORIO = "El teléfono es obligatorio";
    public static final String TELEFONO_RESTAURANTE_INVALIDO =
            "El teléfono debe tener máximo 13 caracteres, solo números y opcionalmente el símbolo +";
    public static final String URL_LOGO_OBLIGATORIA = "La URL del logo es obligatoria";
    public static final String PROPIETARIO_RESTAURANTE_OBLIGATORIO = "El id del propietario es obligatorio";

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

    public static final String TRAZABILIDAD_NO_DISPONIBLE =
            "No fue posible registrar el cambio de estado en el servicio de trazabilidad";

    public static final String ESTADO_PEDIDO_OBLIGATORIO = "El estado del pedido es obligatorio";
    public static final String EMPLEADO_SIN_RESTAURANTE =
            "El empleado no tiene un restaurante asignado";

    public static final String PEDIDO_NO_ENCONTRADO = "No existe el pedido con id ";
    public static final String PEDIDO_DE_OTRO_RESTAURANTE =
            "El pedido no pertenece al restaurante del empleado";
    public static final String PEDIDO_NO_PENDIENTE =
            "Solo se puede tomar un pedido en estado PENDIENTE";

    private MensajeError() {
    }
}