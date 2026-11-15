# Plazoleta (plazoleta-ms)

Microservicio de **restaurantes, platos y pedidos** de la plataforma Plazoleta. Está hecho con **Spring Boot 4.1.1 y Java 21**, sigue la **plantilla de arquitectura hexagonal v2** y se comunica con otros tres microservicios del sistema.

| Servicio | Puerto | Responsabilidad |
|---|---|---|
| usuarios-ms | 8081 | Usuarios, roles y autenticación (emite el JWT) |
| **plazoleta-ms** (este) | **8082** | Restaurantes, platos y pedidos |
| mensajeria-ms | 8083 | Envío de SMS (PIN de entrega) |
| trazabilidad-ms | 8084 | Historial de cambios de estado de los pedidos |

## Requisitos

- Java 21
- No hace falta instalar Maven: se usa el wrapper (`mvnw.cmd` en Windows, `./mvnw` en Linux o macOS)
- Para probar el flujo completo: los otros tres servicios en marcha

## Variables de entorno

| Variable | Obligatoria | Descripción |
|---|---|---|
| `JWT_SECRET` | Sí | Clave para validar el JWT. Mínimo 32 caracteres. **Debe ser idéntica en los cuatro servicios** o el token de uno será rechazado por los demás |
| `USUARIOS_SERVICE_URL` | No | URL base de usuarios-ms. Por defecto `http://localhost:8081/api/v1` |
| `TRAZABILIDAD_SERVICE_URL` | No | URL base de trazabilidad-ms. Por defecto `http://localhost:8084/api/v1` |
| `MENSAJERIA_SERVICE_URL` | No | URL base de mensajeria-ms. Por defecto `http://localhost:8083/api/v1` |

## Cómo correrlo

Windows (PowerShell):

```powershell
$env:JWT_SECRET = "una-clave-larga-de-al-menos-32-caracteres-123"
.\mvnw.cmd spring-boot:run
```

Linux o macOS:

```bash
export JWT_SECRET="una-clave-larga-de-al-menos-32-caracteres-123"
./mvnw spring-boot:run
```

El servicio queda en `http://localhost:8082`.

- Swagger UI: `http://localhost:8082/swagger-ui/index.html`
- OpenAPI en JSON: `http://localhost:8082/v3/api-docs`
- Copia exportada del contrato: [`docs/openapi-plazoleta.json`](docs/openapi-plazoleta.json)

Pruebas:

```powershell
.\mvnw.cmd clean test
```

> La base de datos es **H2 en memoria**: los datos se pierden cada vez que se reinicia el servicio. Después de reiniciar hay que volver a cargar los datos de la demo (ver abajo).

## Autenticación y roles

Todos los endpoints exigen un JWT (`Authorization: Bearer <token>`) emitido por usuarios-ms. Los permisos se definen por rol en `SecurityConfiguration`:

| Rol | Qué puede hacer |
|---|---|
| ADMINISTRADOR | Crear restaurantes |
| PROPIETARIO | Crear, modificar y habilitar o deshabilitar platos de su restaurante |
| CLIENTE | Listar restaurantes y platos, crear pedidos y cancelarlos |
| EMPLEADO | Listar pedidos por estado, tomarlos, marcarlos listos y entregarlos |

### Endpoints de pedidos

| Método y ruta | Rol | Historia | Qué hace |
|---|---|---|---|
| `POST /pedidos` | CLIENTE | HU 11 | Crea un pedido (estado `PENDIENTE`) |
| `GET /pedidos?estado=&pagina=&tamanio=` | EMPLEADO | HU 12 | Lista los pedidos de su restaurante por estado, paginado |
| `PATCH /pedidos/{id}/preparacion` | EMPLEADO | HU 13 | Toma el pedido: `EN_PREPARACION` |
| `PATCH /pedidos/{id}/listo` | EMPLEADO | HU 14 | Marca `LISTO`, genera el PIN y envía el SMS |
| `PATCH /pedidos/{id}/entrega` | EMPLEADO | HU 15 | Entrega con el PIN: `ENTREGADO` |
| `PATCH /pedidos/{id}/cancelacion` | CLIENTE | HU 16 | Cancela solo si está `PENDIENTE` y es suyo |

Estados: `PENDIENTE → EN_PREPARACION → LISTO → ENTREGADO`, o `PENDIENTE → CANCELADO`.

El detalle de los demás endpoints (restaurantes y platos) está en Swagger.

## Arquitectura hexagonal (plantilla v2)

Flujo de una petición: **Controller → Handler → puerto de servicio (`domain/api`) → UseCase → puerto de salida (`domain/spi`) → adaptador**.

```
src/main/java/com/plazoleta/plazoleta/
|-- domain/                  Nucleo: sin dependencias de Spring
|   |-- model/               Objetos de dominio (Pedido, Plato, Restaurante, PaginaRespuesta...)
|   |-- api/                 Puertos de entrada (PedidoServicePort...)
|   |-- spi/                 Puertos de salida (PedidoPersistencePort, UsuarioClientPort...)
|   |-- exception/           Excepciones de dominio
|   `-- util/                MensajeError: todos los mensajes centralizados
|-- application/
|   |-- handler/             Interfaz + implementacion: reciben objetos de dominio
|   `-- usecase/             Solo reglas de negocio
`-- infrastructure/
    |-- input/rest/          controller, dto (con validaciones Jakarta), mapper (MapStruct)
    |-- output/jpa/          adapter, entity, repository, mapper
    |-- output/http/         adaptadores y DTO hacia usuarios, trazabilidad y mensajeria
    |-- output/pin/          Generador del PIN de entrega (SecureRandom)
    |-- config/              SecurityConfiguration, OpenAPI, GlobalExceptionHandler
    |-- security/            Componentes de seguridad JWT
    `-- exception/           Excepciones de infraestructura (servicio caido, etc.)
```

Reglas que se siguen:

- El **dominio no depende de Spring**: la paginación usa un `PaginaRespuesta<T>` propio y los adaptadores JPA convierten desde y hacia `Pageable`.
- Los **UseCase** solo tienen reglas de negocio. Las validaciones de formato van en los DTO de request con anotaciones Jakarta y `@Valid`.
- Los **mensajes de error** están en `MensajeError`, nunca escritos dentro de las clases.
- Lombok se usa solo en infraestructura.
- Los errores salen siempre con el mismo formato desde `GlobalExceptionHandler`: `{ "mensaje": "...", "status": 400 }`.

## Decisiones de diseño

- **Precio de los platos:** en el request llega como `BigDecimal` y se valida que sea positivo y entero; en dominio, entidad y respuesta es `Integer`. Así un precio como `10.5` se rechaza con un mensaje claro en lugar de truncarse en silencio.
- **PIN de entrega:** nunca aparece en ninguna respuesta de la API. Solo viaja por SMS y se compara en la entrega.
- **Orden en "listo":** el SMS se envía antes de guardar. Si mensajería falla, la respuesta es 503 y el pedido sigue `EN_PREPARACION`, por lo que se puede reintentar.
- **Trazabilidad:** cada cambio de estado se registra en trazabilidad-ms. Si ese servicio falla después de guardar, se responde 503 y el pedido queda guardado (no hay rollback entre servicios; es una limitación conocida).

## Datos y guion de la demo

En `docs/demo/`:

- `cargar-demo.ps1`: crea administrador, propietario, 2 restaurantes con platos, empleado y clientes. Es idempotente.
- `pruebas-errores.ps1`: ejecuta los casos de error y de negocio contra los servicios en marcha y reporta PASS o FAIL.
- `GUION_DEMO.md`: guion paso a paso de la presentación.

Orden para correr la demo: levantar los cuatro servicios con el mismo `JWT_SECRET`, luego

```powershell
powershell -ExecutionPolicy Bypass -File .\docs\demo\cargar-demo.ps1 -CelularCliente "+57XXXXXXXXXX"
powershell -ExecutionPolicy Bypass -File .\docs\demo\pruebas-errores.ps1
```
