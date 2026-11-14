# Guion de la demo — Plazoleta

## 1. Preparación (antes de presentar)

Los cuatro servicios deben compartir el mismo `JWT_SECRET` (mínimo 32 caracteres). En cada ventana de PowerShell:

```powershell
$env:JWT_SECRET = "una-clave-larga-de-al-menos-32-caracteres-123"
.\mvnw.cmd spring-boot:run
```

| Servicio | Puerto | Hace falta para el script |
|---|---|---|
| usuarios-ms | 8081 | sí |
| plazoleta-ms | 8082 | sí |
| mensajeria-ms | 8083 | no, pero sí para el SMS de HU 14 |
| trazabilidad-ms | 8084 | no, pero sí para el historial |

Las bases de datos son H2 en memoria: **cada reinicio las vacía**. Después de levantar los servicios, carga los datos:

```powershell
powershell -ExecutionPolicy Bypass -File .\docs\demo\cargar-demo.ps1 -CelularCliente "+57XXXXXXXXXX"
```

`-CelularCliente` es el celular del cliente 1, el que recibirá el SMS (13 caracteres como máximo, dígitos con `+` opcional). Si mensajería está en modo simulado, el SMS solo aparece en su log.

El script imprime los ids y deja los tokens en `docs/demo/resumen-demo.local.txt` (ignorado por git). Abre Swagger en `http://localhost:8082/swagger-ui/index.html`, pulsa **Authorize** y pega el token del rol que toque, sin la palabra `Bearer`.

## 2. Cuentas de la demo

| Rol | Correo | Clave | Detalle |
|---|---|---|---|
| ADMINISTRADOR | admin@plazoleta.com | Admin123* | sembrado por usuarios-ms |
| PROPIETARIO | propietario@plazoleta.com | Demo123* | dueño de los 2 restaurantes |
| EMPLEADO 1 | empleado1@plazoleta.com | Demo123* | restaurante 1 (Sabor 24) |
| EMPLEADO 2 | empleado2@plazoleta.com | Demo123* | restaurante 2 (La Parrilla Dorada) |
| CLIENTE 1 | cliente1@plazoleta.com | Demo123* | recibe el SMS |
| CLIENTE 2 | cliente2@plazoleta.com | Demo123* | para la cancelación ajena |

Login: `POST http://localhost:8081/api/v1/auth/login` con `{"correo":"...","clave":"..."}`; el token viene en el campo `token`.

## 3. Datos cargados

- **Sabor 24**: Bandeja paisa, Sancocho de gallina (plato fuerte); Empanadas x3, Arepa con queso (entrada); Tres leches (postre); Limonada de coco (bebida).
- **La Parrilla Dorada**: Churrasco, Pechuga a la plancha (plato fuerte); Papas criollas, Ensalada de la casa (entrada); Brownie con helado (postre); Jugo natural (bebida).

Los ids reales de restaurantes y platos están en la tabla que imprime el script. Tras un arranque limpio suelen ser 1 y 2 para los restaurantes y del 1 al 12 para los platos.

## 4. Recorrido sugerido

**Restaurantes y platos (HU 1 a 10)**
1. *Cliente 1*: `GET /restaurantes` (paginado, solo nombre y logo).
2. *Cliente 1*: `GET /platos/restaurante/{id}`, primero sin filtro y luego con `categoria=Postre`. Mostrar que solo salen platos activos.
3. *Propietario*: deshabilitar un plato con `PATCH /platos/{id}/estado` `{"activo":false}` y comprobar que desaparece del menú del cliente. Volver a habilitarlo.

**Pedido (HU 11)**
4. *Cliente 1*: `POST /pedidos` con `{"idRestaurante":1,"platos":[{"idPlato":1,"cantidad":2},{"idPlato":5,"cantidad":1}]}` (usar los ids reales). Queda `PENDIENTE`.
5. *Cliente 1*: repetir el `POST`. Responde 400: ya tiene un pedido en proceso.
6. Verificar en trazabilidad-ms el primer registro (sin estado anterior, nuevo `PENDIENTE`).

**Empleado (HU 12 y 13)**
7. *Empleado 1*: `GET /pedidos?estado=PENDIENTE`. Aparece el pedido (el listado nunca muestra el pin).
8. *Empleado 2*: el mismo `GET` sale vacío (es de otro restaurante).
9. *Empleado 2*: `PATCH /pedidos/{id}/preparacion` responde 400: el pedido no es de su restaurante.
10. *Empleado 1*: `PATCH /pedidos/{id}/preparacion`. Pasa a `EN_PREPARACION`. Repetirlo responde 400.

**Cancelación (HU 16)**
11. *Cliente 1*: `PATCH /pedidos/{id}/cancelacion` responde 400 con «Lo sentimos, tu pedido ya está en preparación y no puede cancelarse».
12. *Cliente 2*: `POST /pedidos` en el restaurante 2 (queda `PENDIENTE`).
13. *Cliente 1*: `PATCH /pedidos/{id del cliente 2}/cancelacion` responde 403 (no es suyo).
14. *Cliente 2*: `PATCH /pedidos/{su id}/cancelacion` responde 200 con `CANCELADO`.

**Pedido listo y entrega (HU 14 y 15)** — *completar con las rutas finales de Reinel*
15. *Empleado 1*: `PATCH /pedidos/{id}/listo` sobre el pedido del paso 10. Responde 200 con estado `LISTO` y sin `pin`. Se genera el PIN y mensajeria-ms envía el SMS al celular del cliente 1. Con mensajería en modo simulado el PIN aparece en el log de ese servicio. Con mensajería apagada responde 503 y el pedido queda LISTO (limitación documentada).
16. *Empleado 1*: entregar con un PIN incorrecto (debe fallar) y luego con el PIN del SMS (pasa a `ENTREGADO`).
17. Mostrar el historial completo del pedido en trazabilidad-ms.

## 5. Si algo sale mal

- **El script dice que ya existen los restaurantes**: reiniciar plazoleta-ms y volver a correr el script.
- **Login responde 401**: usuarios-ms se reinició y perdió el administrador sembrado, o la clave cambió. Reiniciarlo.
- **Un servicio responde 401 con tokens válidos**: los servicios no tienen el mismo `JWT_SECRET`.
- **Responde 503 al marcar un pedido como listo**: mensajería o trazabilidad no están corriendo.
- **Los tokens vencieron**: volver a correr el script; inicia sesión de nuevo y regenera `resumen-demo.local.txt`.