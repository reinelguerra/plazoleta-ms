<#
.SYNOPSIS
  Casos de error de la API contra los servicios reales: PASS o FAIL por cada caso.

.DESCRIPTION
  Correr JUSTO despues de cargar-demo.ps1 con datos recien cargados.
  Necesita corriendo: usuarios-ms (8081), plazoleta-ms (8082) y trazabilidad-ms (8084),
  porque crear un pedido registra el log en trazabilidad. La seccion D necesita ademas mensajeria-ms (8083).
  Deja pedidos creados: para repetirlo hay que reiniciar plazoleta-ms y volver a cargar los datos.

.EXAMPLE
  powershell -ExecutionPolicy Bypass -File .\docs\demo\pruebas-errores.ps1
#>
[CmdletBinding()]
param(
    [string]$UrlUsuarios = "http://localhost:8081/api/v1",
    [string]$UrlPlazoleta = "http://localhost:8082/api/v1",
    [long]$IdRestaurante1 = 1,
    [long]$IdRestaurante2 = 2
)

$ErrorActionPreference = "Stop"
$Clave = "Demo123*"
$resultados = New-Object System.Collections.Generic.List[object]

function Leer-Json($texto) {
    if ([string]::IsNullOrWhiteSpace($texto)) { return $null }
    try { return ($texto | ConvertFrom-Json) } catch { return $null }
}

function Invocar([string]$Metodo, [string]$Url, $Cuerpo = $null, [string]$Token = $null) {
    $parametros = @{ Method = $Metodo; Uri = $Url; UseBasicParsing = $true }
    if ($Token) { $parametros.Headers = @{ Authorization = "Bearer $Token" } }
    if ($null -ne $Cuerpo) {
        $json = $Cuerpo
        if ($Cuerpo -isnot [string]) { $json = $Cuerpo | ConvertTo-Json -Depth 6 }
        $parametros.Body = [Text.Encoding]::UTF8.GetBytes($json)
        $parametros.ContentType = "application/json; charset=utf-8"
    }
    try {
        $r = Invoke-WebRequest @parametros
        $texto = [Text.Encoding]::UTF8.GetString($r.RawContentStream.ToArray())
        return [pscustomobject]@{ Status = [int]$r.StatusCode; Json = (Leer-Json $texto) }
    } catch [System.Net.WebException] {
        $resp = $_.Exception.Response
        if ($null -eq $resp) { throw "No pude conectar con ${Url}. Esta corriendo ese servicio?" }
        $lector = New-Object IO.StreamReader($resp.GetResponseStream(), [Text.Encoding]::UTF8)
        $texto = $lector.ReadToEnd()
        $lector.Close()
        return [pscustomobject]@{ Status = [int]$resp.StatusCode; Json = (Leer-Json $texto) }
    }
}

function Iniciar-Sesion([string]$Correo, [string]$ClaveUsuario) {
    $r = Invocar "POST" "$UrlUsuarios/auth/login" @{ correo = $Correo; clave = $ClaveUsuario }
    if ($r.Status -ne 200 -or -not $r.Json.token) { throw "Login de $Correo fallo (HTTP $($r.Status)). Corriste cargar-demo.ps1?" }
    return [string]$r.Json.token
}

function Verificar([string]$Nombre, [int]$Esperado, $Respuesta, [string]$Contiene = "") {
    $mensaje = ""
    if ($Respuesta.Json -and $Respuesta.Json.mensaje) { $mensaje = [string]$Respuesta.Json.mensaje }
    $ok = ($Respuesta.Status -eq $Esperado)
    if ($ok -and $Contiene -ne "" -and ($mensaje -notlike "*$Contiene*")) { $ok = $false }
    $resultados.Add([pscustomobject]@{ Caso = $Nombre; Ok = $ok })
    $etiqueta = "FAIL"; $color = "Red"
    if ($ok) { $etiqueta = "PASS"; $color = "Green" }
    Write-Host ("{0}  {1}  [esperado {2}, obtenido {3}]  {4}" -f $etiqueta, $Nombre, $Esperado, $Respuesta.Status, $mensaje) -ForegroundColor $color
}

Write-Host "Iniciando sesion con cada rol..."
$tAdmin = Iniciar-Sesion "admin@plazoleta.com" "Admin123*"
$tProp = Iniciar-Sesion "propietario@plazoleta.com" $Clave
$tEmp1 = Iniciar-Sesion "empleado1@plazoleta.com" $Clave
$tEmp2 = Iniciar-Sesion "empleado2@plazoleta.com" $Clave
$tCli1 = Iniciar-Sesion "cliente1@plazoleta.com" $Clave
$tCli2 = Iniciar-Sesion "cliente2@plazoleta.com" $Clave

function Primer-Plato([long]$IdRestaurante) {
    $m = Invocar "GET" "$UrlPlazoleta/platos/restaurante/${IdRestaurante}?pagina=0&tamanio=50" $null $tCli1
    if ($m.Status -ne 200 -or @($m.Json.contenido).Count -eq 0) { throw "No encontre platos en el restaurante $IdRestaurante" }
    return [long]@($m.Json.contenido)[0].id
}
$plato1 = Primer-Plato $IdRestaurante1
$plato2 = Primer-Plato $IdRestaurante2
$pedido1 = '{"idRestaurante":' + $IdRestaurante1 + ',"platos":[{"idPlato":' + $plato1 + ',"cantidad":2}]}'
$pedido2 = '{"idRestaurante":' + $IdRestaurante2 + ',"platos":[{"idPlato":' + $plato2 + ',"cantidad":1}]}'
$platoBase = '"nombre":"Prueba","descripcion":"d","urlImagen":"http://img.png","categoria":"c","idRestaurante":' + $IdRestaurante1

Write-Host ""
Write-Host "== A. Autenticacion y roles =="
Verificar "Sin token: listar pedidos" 401 (Invocar "GET" "$UrlPlazoleta/pedidos?estado=PENDIENTE")
Verificar "Cliente no lista pedidos de empleado" 403 (Invocar "GET" "$UrlPlazoleta/pedidos?estado=PENDIENTE" $null $tCli1)
Verificar "Empleado no crea pedidos" 403 (Invocar "POST" "$UrlPlazoleta/pedidos" $pedido1 $tEmp1)
Verificar "Cliente no crea platos" 403 (Invocar "POST" "$UrlPlazoleta/platos" ('{' + $platoBase + ',"precio":100}') $tCli1)
Verificar "Propietario no crea restaurantes" 403 (Invocar "POST" "$UrlPlazoleta/restaurantes" '{"nombre":"X"}' $tProp)
Verificar "Cliente no toma pedidos" 403 (Invocar "PATCH" "$UrlPlazoleta/pedidos/1/preparacion" $null $tCli1)
Verificar "Empleado no cancela pedidos" 403 (Invocar "PATCH" "$UrlPlazoleta/pedidos/1/cancelacion" $null $tEmp1)
Verificar "Empleado no lista platos del cliente" 403 (Invocar "GET" "$UrlPlazoleta/platos/restaurante/${IdRestaurante1}" $null $tEmp1)

Write-Host ""
Write-Host "== B. Validaciones de los DTO =="
Verificar "Plato con precio 0" 400 (Invocar "POST" "$UrlPlazoleta/platos" ('{' + $platoBase + ',"precio":0}') $tProp) "precio"
Verificar "Plato con precio decimal" 400 (Invocar "POST" "$UrlPlazoleta/platos" ('{' + $platoBase + ',"precio":10.5}') $tProp) "precio"
Verificar "Plato sin precio" 400 (Invocar "POST" "$UrlPlazoleta/platos" ('{' + $platoBase + '}') $tProp) "precio"
Verificar "Plato con nombre vacio" 400 (Invocar "POST" "$UrlPlazoleta/platos" ('{"nombre":" ","precio":100,"descripcion":"d","urlImagen":"u","categoria":"c","idRestaurante":' + $IdRestaurante1 + '}') $tProp) "nombre"
Verificar "Cambiar estado sin el campo activo" 400 (Invocar "PATCH" "$UrlPlazoleta/platos/$plato1/estado" '{}' $tProp) "estado"
Verificar "Pedido sin platos" 400 (Invocar "POST" "$UrlPlazoleta/pedidos" ('{"idRestaurante":' + $IdRestaurante1 + ',"platos":[]}') $tCli1) "plato"
Verificar "Pedido con cantidad 0" 400 (Invocar "POST" "$UrlPlazoleta/pedidos" ('{"idRestaurante":' + $IdRestaurante1 + ',"platos":[{"idPlato":' + $plato1 + ',"cantidad":0}]}') $tCli1) "cantidad"
Verificar "Restaurante con NIT no numerico" 400 (Invocar "POST" "$UrlPlazoleta/restaurantes" '{"nombre":"Rest","nit":"ABC","direccion":"d","telefono":"+573001234567","urlLogo":"u","idPropietario":1}' $tAdmin) "NIT"
Verificar "Listar pedidos sin estado" 400 (Invocar "GET" "$UrlPlazoleta/pedidos" $null $tEmp1)
Verificar "Listar pedidos con estado inexistente" 400 (Invocar "GET" "$UrlPlazoleta/pedidos?estado=COCINANDO" $null $tEmp1)
Verificar "Listar platos con pagina negativa" 400 (Invocar "GET" "$UrlPlazoleta/platos/restaurante/${IdRestaurante1}?pagina=-1" $null $tCli1)

Write-Host ""
Write-Host "== C. Reglas de negocio =="
$crear1 = Invocar "POST" "$UrlPlazoleta/pedidos" $pedido1 $tCli1
Verificar "Cliente 1 crea su pedido" 201 $crear1
if ($crear1.Status -ne 201) { throw "No se pudo crear el pedido base; revisa trazabilidad-ms y que los datos esten recien cargados" }
$idPedido1 = [long]$crear1.Json.id
Verificar "Cliente con pedido en proceso" 400 (Invocar "POST" "$UrlPlazoleta/pedidos" $pedido1 $tCli1) "en proceso"
Verificar "Cancelar un pedido ajeno" 403 (Invocar "PATCH" "$UrlPlazoleta/pedidos/$idPedido1/cancelacion" $null $tCli2)
Verificar "Empleado de otro restaurante" 400 (Invocar "PATCH" "$UrlPlazoleta/pedidos/$idPedido1/preparacion" $null $tEmp2) "restaurante"
Verificar "Pedido inexistente (tomar)" 404 (Invocar "PATCH" "$UrlPlazoleta/pedidos/987654321/preparacion" $null $tEmp1)
Verificar "Pedido inexistente (cancelar)" 404 (Invocar "PATCH" "$UrlPlazoleta/pedidos/987654321/cancelacion" $null $tCli1)
Verificar "Empleado toma su pedido" 200 (Invocar "PATCH" "$UrlPlazoleta/pedidos/$idPedido1/preparacion" $null $tEmp1)
Verificar "Tomar un pedido ya en preparacion" 400 (Invocar "PATCH" "$UrlPlazoleta/pedidos/$idPedido1/preparacion" $null $tEmp1) "PENDIENTE"
Verificar "Cancelar fuera de PENDIENTE" 400 (Invocar "PATCH" "$UrlPlazoleta/pedidos/$idPedido1/cancelacion" $null $tCli1) "no puede cancelarse"
$crear2 = Invocar "POST" "$UrlPlazoleta/pedidos" $pedido2 $tCli2
Verificar "Cliente 2 crea su pedido" 201 $crear2
if ($crear2.Status -eq 201) {
    $idPedido2 = [long]$crear2.Json.id
    Verificar "Cliente 2 cancela su pedido PENDIENTE" 200 (Invocar "PATCH" "$UrlPlazoleta/pedidos/$idPedido2/cancelacion" $null $tCli2)
    Verificar "Cancelar un pedido ya cancelado" 400 (Invocar "PATCH" "$UrlPlazoleta/pedidos/$idPedido2/cancelacion" $null $tCli2)
}

function Anotar([string]$Nombre, [bool]$Ok) {
    $resultados.Add([pscustomobject]@{ Caso = $Nombre; Ok = $Ok })
    $etiqueta = "FAIL"; $color = "Red"
    if ($Ok) { $etiqueta = "PASS"; $color = "Green" }
    Write-Host ("{0}  {1}" -f $etiqueta, $Nombre) -ForegroundColor $color
}

Write-Host ""
Write-Host "== D. HU 14: marcar como listo (SMS con PIN) =="
Verificar "Sin token: marcar listo" 401 (Invocar "PATCH" "$UrlPlazoleta/pedidos/$idPedido1/listo")
Verificar "Cliente no marca listo" 403 (Invocar "PATCH" "$UrlPlazoleta/pedidos/$idPedido1/listo" $null $tCli1)
Verificar "Pedido inexistente (listo)" 404 (Invocar "PATCH" "$UrlPlazoleta/pedidos/987654321/listo" $null $tEmp1)
Verificar "Empleado de otro restaurante (listo)" 400 (Invocar "PATCH" "$UrlPlazoleta/pedidos/$idPedido1/listo" $null $tEmp2) "no pertenece al restaurante"
$crear3 = Invocar "POST" "$UrlPlazoleta/pedidos" $pedido2 $tCli2
Verificar "Cliente 2 crea otro pedido" 201 $crear3
if ($crear3.Status -eq 201) {
    $idPedido3 = [long]$crear3.Json.id
    Verificar "Marcar listo un pedido PENDIENTE" 400 (Invocar "PATCH" "$UrlPlazoleta/pedidos/$idPedido3/listo" $null $tEmp2) "Solo se puede marcar como listo"
}
$listo = Invocar "PATCH" "$UrlPlazoleta/pedidos/$idPedido1/listo" $null $tEmp1
Verificar "Empleado marca listo su pedido (envia el SMS)" 200 $listo
if ($listo.Status -eq 200) {
    Anotar "La respuesta de listo trae estado LISTO" ($listo.Json.estado -eq "LISTO")
    Anotar "La respuesta de listo NO expone el pin" (-not ($listo.Json.PSObject.Properties.Name -contains "pin"))
}
Verificar "Marcar listo un pedido ya LISTO" 400 (Invocar "PATCH" "$UrlPlazoleta/pedidos/$idPedido1/listo" $null $tEmp1) "Solo se puede marcar como listo"
Verificar "Cancelar un pedido LISTO" 400 (Invocar "PATCH" "$UrlPlazoleta/pedidos/$idPedido1/cancelacion" $null $tCli1) "no puede cancelarse"
$lista = Invocar "GET" "$UrlPlazoleta/pedidos?estado=LISTO" $null $tEmp1
$enLista = $false; $conPin = $false
if ($lista.Status -eq 200 -and $lista.Json) {
    foreach ($p in @($lista.Json.contenido)) {
        if ([long]$p.id -eq $idPedido1) { $enLista = $true }
        if ($p.PSObject.Properties.Name -contains "pin") { $conPin = $true }
    }
}
Anotar "El listado ?estado=LISTO incluye el pedido" $enLista
Anotar "El listado no expone el pin" (-not $conPin)

Write-Host ""
Write-Host "== E. HU 15: entregar pedido con PIN =="
$urlEntrega = "$UrlPlazoleta/pedidos/$idPedido1/entrega"
Verificar "Sin token: entregar" 401 (Invocar "PATCH" $urlEntrega @{ pin = "123456" })
Verificar "Cliente no entrega" 403 (Invocar "PATCH" $urlEntrega @{ pin = "123456" } $tCli1)
Verificar "Pedido inexistente (entrega)" 404 (Invocar "PATCH" "$UrlPlazoleta/pedidos/987654321/entrega" @{ pin = "123456" } $tEmp1)
Verificar "PIN vacio" 400 (Invocar "PATCH" $urlEntrega @{ pin = "" } $tEmp1) "PIN de entrega es obligatorio"
Verificar "PIN con menos de 6 digitos" 400 (Invocar "PATCH" $urlEntrega @{ pin = "12" } $tEmp1) "PIN de entrega es obligatorio"
Verificar "Empleado de otro restaurante (entrega)" 400 (Invocar "PATCH" $urlEntrega @{ pin = "123456" } $tEmp2) "no pertenece al restaurante"
if ($idPedido3) {
    Verificar "Entregar un pedido que no esta LISTO" 400 (Invocar "PATCH" "$UrlPlazoleta/pedidos/$idPedido3/entrega" @{ pin = "123456" } $tEmp2) "Solo se puede entregar"
}
Verificar "PIN incorrecto" 400 (Invocar "PATCH" $urlEntrega @{ pin = "000000" } $tEmp1) "no es correcto"
$pinDemo = Read-Host "PIN del pedido $idPedido1 (esta en el log de mensajeria-ms o llego por SMS; Enter para omitir)"
if ($pinDemo) {
    $entrega = Invocar "PATCH" $urlEntrega @{ pin = $pinDemo } $tEmp1
    Verificar "PIN correcto: pedido entregado" 200 $entrega
    if ($entrega.Status -eq 200) {
        Anotar "La respuesta de entrega trae estado ENTREGADO" ($entrega.Json.estado -eq "ENTREGADO")
        Anotar "La respuesta de entrega NO expone el pin" (-not ($entrega.Json.PSObject.Properties.Name -contains "pin"))
    }
    Verificar "Entregar un pedido ya ENTREGADO" 400 (Invocar "PATCH" $urlEntrega @{ pin = $pinDemo } $tEmp1) "Solo se puede entregar"
    Verificar "Cancelar un pedido ENTREGADO" 400 (Invocar "PATCH" "$UrlPlazoleta/pedidos/$idPedido1/cancelacion" $null $tCli1) "no puede cancelarse"
} else {
    Write-Host "(Camino feliz de entrega omitido: no se escribio un PIN)" -ForegroundColor Yellow
}

$fallos = @($resultados | Where-Object { -not $_.Ok })
Write-Host ""
if ($fallos.Count -eq 0) {
    Write-Host "TODOS LOS CASOS PASARON ($($resultados.Count))" -ForegroundColor Green
} else {
    Write-Host "$($fallos.Count) de $($resultados.Count) casos FALLARON:" -ForegroundColor Red
    $fallos | ForEach-Object { Write-Host "  - $($_.Caso)" -ForegroundColor Red }
    exit 1
}