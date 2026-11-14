<#
.SYNOPSIS
  Carga los datos de la demo de Plazoleta llamando a las APIs reales.

.DESCRIPTION
  Necesita usuarios-ms (8081) y plazoleta-ms (8082) corriendo con el mismo JWT_SECRET.
  Crea: propietario, 2 restaurantes con platos, 2 empleados (uno por restaurante) y 2 clientes.
  Las bases estan en memoria: hay que correrlo cada vez que se reinicien los servicios.
  Si usuarios-ms sigue vivo, los usuarios que ya existan (409) se reutilizan.
  Si plazoleta-ms sigue vivo con los restaurantes ya creados, hay que reiniciarlo.

.EXAMPLE
  powershell -ExecutionPolicy Bypass -File .\docs\demo\cargar-demo.ps1 -CelularCliente "+573001234567"
#>
[CmdletBinding()]
param(
    [ValidatePattern('^\+?\d{1,12}$')]
    [string]$CelularCliente = "+573001234567",
    [string]$UrlUsuarios = "http://localhost:8081/api/v1",
    [string]$UrlPlazoleta = "http://localhost:8082/api/v1",
    [string]$AdminCorreo = "admin@plazoleta.com",
    [string]$AdminClave = "Admin123*"
)

$ErrorActionPreference = "Stop"
$ClaveDemo = "Demo123*"

function Leer-Json($texto) {
    if ([string]::IsNullOrWhiteSpace($texto)) { return $null }
    try { return ($texto | ConvertFrom-Json) } catch { return $null }
}

function Invocar([string]$Metodo, [string]$Url, $Cuerpo = $null, [string]$Token = $null) {
    $parametros = @{ Method = $Metodo; Uri = $Url; UseBasicParsing = $true }
    if ($Token) { $parametros.Headers = @{ Authorization = "Bearer $Token" } }
    if ($null -ne $Cuerpo) {
        $json = $Cuerpo | ConvertTo-Json -Depth 6
        $parametros.Body = [Text.Encoding]::UTF8.GetBytes($json)
        $parametros.ContentType = "application/json; charset=utf-8"
    }
    try {
        $r = Invoke-WebRequest @parametros
        $texto = [Text.Encoding]::UTF8.GetString($r.RawContentStream.ToArray())
        return [pscustomobject]@{ Status = [int]$r.StatusCode; Texto = $texto; Json = (Leer-Json $texto) }
    } catch [System.Net.WebException] {
        $resp = $_.Exception.Response
        if ($null -eq $resp) {
            throw "No pude conectar con ${Url}. Esta corriendo ese servicio?"
        }
        $lector = New-Object IO.StreamReader($resp.GetResponseStream(), [Text.Encoding]::UTF8)
        $texto = $lector.ReadToEnd()
        $lector.Close()
        return [pscustomobject]@{ Status = [int]$resp.StatusCode; Texto = $texto; Json = (Leer-Json $texto) }
    }
}

function Fallar([string]$Contexto, $Respuesta) {
    $detalle = $Respuesta.Texto
    if ($Respuesta.Json -and $Respuesta.Json.mensaje) { $detalle = $Respuesta.Json.mensaje }
    throw "$Contexto - HTTP $($Respuesta.Status): $detalle"
}

function Leer-IdDelToken([string]$Token) {
    $carga = $Token.Split('.')[1].Replace('-', '+').Replace('_', '/')
    switch ($carga.Length % 4) { 2 { $carga += '==' } 3 { $carga += '=' } }
    $json = [Text.Encoding]::UTF8.GetString([Convert]::FromBase64String($carga))
    return [long](($json | ConvertFrom-Json).id)
}

function Iniciar-Sesion([string]$Correo, [string]$Clave) {
    $r = Invocar "POST" "$UrlUsuarios/auth/login" @{ correo = $Correo; clave = $Clave }
    if ($r.Status -ne 200 -or -not $r.Json -or -not $r.Json.token) { Fallar "Login de $Correo" $r }
    return [string]$r.Json.token
}

function Asegurar-Usuario([string]$Ruta, $Cuerpo, [string]$TokenCreador) {
    $r = Invocar "POST" "$UrlUsuarios$Ruta" $Cuerpo $TokenCreador
    if ($r.Status -eq 201) {
        Write-Host "   creado     $($Cuerpo.correo)"
        return [long]$r.Json.id
    }
    if ($r.Status -eq 409) {
        Write-Host "   ya existia $($Cuerpo.correo) (se reutiliza)"
        return (Leer-IdDelToken (Iniciar-Sesion $Cuerpo.correo $Cuerpo.clave))
    }
    Fallar "Crear $($Cuerpo.correo)" $r
}

function Crear-Restaurante($Cuerpo, [string]$Token) {
    $r = Invocar "POST" "$UrlPlazoleta/restaurantes" $Cuerpo $Token
    if ($r.Status -ne 201) {
        Fallar "Crear el restaurante '$($Cuerpo.nombre)'. Si ya cargaste los datos en este plazoleta-ms, reinicialo (su base es en memoria) y vuelve a correr el script" $r
    }
    Write-Host "   restaurante $($r.Json.id): $($Cuerpo.nombre)"
    return [long]$r.Json.id
}

function Crear-Plato($Cuerpo, [string]$Token) {
    $r = Invocar "POST" "$UrlPlazoleta/platos" $Cuerpo $Token
    if ($r.Status -ne 201) { Fallar "Crear el plato '$($Cuerpo.nombre)'" $r }
    return [pscustomobject]@{ Id = [long]$r.Json.id; Nombre = $Cuerpo.nombre; Precio = $Cuerpo.precio
                              Categoria = $Cuerpo.categoria; IdRestaurante = $Cuerpo.idRestaurante }
}

# ---------------------------------------------------------------- 1. Administrador
Write-Host "1. Administrador"
$tokenAdmin = Iniciar-Sesion $AdminCorreo $AdminClave
Write-Host "   sesion iniciada como $AdminCorreo"

# ---------------------------------------------------------------- 2. Propietario
Write-Host "2. Propietario"
$propietario = @{
    nombre = "Carlos"; apellido = "Propietario"; documentoIdentidad = "71234567"
    celular = "+573001112233"; fechaNacimiento = "1985-03-15"
    correo = "propietario@plazoleta.com"; clave = $ClaveDemo
}
$idPropietario = Asegurar-Usuario "/usuarios/propietarios" $propietario $tokenAdmin
$tokenPropietario = Iniciar-Sesion $propietario.correo $propietario.clave

# ---------------------------------------------------------------- 3. Restaurantes
Write-Host "3. Restaurantes (los crea el administrador)"
$idSabor = Crear-Restaurante @{
    nombre = "Sabor 24"; nit = "900123456"; direccion = "Calle 10 # 5-20"
    telefono = "+573001234567"; urlLogo = "https://example.com/logos/sabor24.png"
    idPropietario = $idPropietario
} $tokenAdmin
$idParrilla = Crear-Restaurante @{
    nombre = "La Parrilla Dorada"; nit = "900654321"; direccion = "Carrera 15 # 80-12"
    telefono = "+573007654321"; urlLogo = "https://example.com/logos/parrilla.png"
    idPropietario = $idPropietario
} $tokenAdmin

# ---------------------------------------------------------------- 4. Platos
Write-Host "4. Platos (los crea el propietario)"
$catalogo = @(
    @{ r = $idSabor;   nombre = "Bandeja paisa";        precio = 28000; categoria = "Plato fuerte"; d = "Frijoles, arroz, carne molida, chicharron, huevo y aguacate" }
    @{ r = $idSabor;   nombre = "Sancocho de gallina";  precio = 24000; categoria = "Plato fuerte"; d = "Sopa tradicional con presa de gallina, yuca y platano" }
    @{ r = $idSabor;   nombre = "Empanadas x3";         precio = 9000;  categoria = "Entrada";      d = "Tres empanadas de carne con aji" }
    @{ r = $idSabor;   nombre = "Arepa con queso";      precio = 7000;  categoria = "Entrada";      d = "Arepa de maiz asada con queso derretido" }
    @{ r = $idSabor;   nombre = "Tres leches";          precio = 12000; categoria = "Postre";       d = "Torta humeda de tres leches" }
    @{ r = $idSabor;   nombre = "Limonada de coco";     precio = 8000;  categoria = "Bebida";       d = "Limonada cremosa con coco" }
    @{ r = $idParrilla; nombre = "Churrasco";           precio = 35000; categoria = "Plato fuerte"; d = "Corte de res a la parrilla con papas y ensalada" }
    @{ r = $idParrilla; nombre = "Pechuga a la plancha"; precio = 26000; categoria = "Plato fuerte"; d = "Pechuga jugosa con arroz y verduras salteadas" }
    @{ r = $idParrilla; nombre = "Papas criollas";       precio = 8000;  categoria = "Entrada";      d = "Papas criollas doradas con hogao" }
    @{ r = $idParrilla; nombre = "Ensalada de la casa";  precio = 11000; categoria = "Entrada";      d = "Lechuga, tomate, zanahoria y vinagreta" }
    @{ r = $idParrilla; nombre = "Brownie con helado";   precio = 13000; categoria = "Postre";       d = "Brownie tibio con helado de vainilla" }
    @{ r = $idParrilla; nombre = "Jugo natural";         precio = 7000;  categoria = "Bebida";       d = "Jugo de fruta de temporada" }
)
$platos = @()
foreach ($p in $catalogo) {
    $platos += Crear-Plato @{
        nombre = $p.nombre; precio = [int]$p.precio; descripcion = $p.d
        urlImagen = "https://example.com/platos/" + ($p.nombre.ToLower() -replace '[^a-z0-9]+', '-') + ".png"
        categoria = $p.categoria; idRestaurante = [long]$p.r
    } $tokenPropietario
}
Write-Host "   $($platos.Count) platos creados"

# ---------------------------------------------------------------- 5. Empleados
Write-Host "5. Empleados (los crea el propietario)"
$empleado1 = @{
    nombre = "Eva"; apellido = "Chef"; documentoIdentidad = "81234567"; celular = "+573004445566"
    correo = "empleado1@plazoleta.com"; clave = $ClaveDemo; idRestaurante = $idSabor
}
$empleado2 = @{
    nombre = "Mario"; apellido = "Parrillero"; documentoIdentidad = "81234568"; celular = "+573004445577"
    correo = "empleado2@plazoleta.com"; clave = $ClaveDemo; idRestaurante = $idParrilla
}
$idEmpleado1 = Asegurar-Usuario "/usuarios/empleados" $empleado1 $tokenPropietario
$idEmpleado2 = Asegurar-Usuario "/usuarios/empleados" $empleado2 $tokenPropietario

# ---------------------------------------------------------------- 6. Clientes
Write-Host "6. Clientes (registro publico)"
$cliente1 = @{
    nombre = "Ana"; apellido = "Cliente"; documentoIdentidad = "621234567"; celular = $CelularCliente
    correo = "cliente1@plazoleta.com"; clave = $ClaveDemo
}
$cliente2 = @{
    nombre = "Luis"; apellido = "Comensal"; documentoIdentidad = "621234568"; celular = "+573011234567"
    correo = "cliente2@plazoleta.com"; clave = $ClaveDemo
}
$idCliente1 = Asegurar-Usuario "/usuarios/clientes" $cliente1 $null
$idCliente2 = Asegurar-Usuario "/usuarios/clientes" $cliente2 $null

# ---------------------------------------------------------------- 7. Tokens frescos
Write-Host "7. Iniciando sesion con cada rol"
$tokenEmpleado1 = Iniciar-Sesion $empleado1.correo $empleado1.clave
$tokenEmpleado2 = Iniciar-Sesion $empleado2.correo $empleado2.clave
$tokenCliente1 = Iniciar-Sesion $cliente1.correo $cliente1.clave
$tokenCliente2 = Iniciar-Sesion $cliente2.correo $cliente2.clave

# ---------------------------------------------------------------- 8. Comprobacion (no fatal)
Write-Host "8. Comprobacion rapida con el token del cliente"
$lista = Invocar "GET" "$UrlPlazoleta/restaurantes?pagina=0&tamanio=10" $null $tokenCliente1
if ($lista.Status -eq 200 -and $lista.Json) {
    Write-Host "   GET /restaurantes: $(@($lista.Json.contenido).Count) restaurante(s)"
} else {
    Write-Host "   AVISO: GET /restaurantes respondio HTTP $($lista.Status) (revisalo a mano en Swagger)"
}
$menu = Invocar "GET" "$UrlPlazoleta/platos/restaurante/${idSabor}?pagina=0&tamanio=20" $null $tokenCliente1
if ($menu.Status -eq 200 -and $menu.Json) {
    Write-Host "   GET /platos/restaurante/${idSabor}: $(@($menu.Json.contenido).Count) plato(s) activo(s)"
} else {
    Write-Host "   AVISO: GET de platos respondio HTTP $($menu.Status) (revisalo a mano en Swagger)"
}

# ---------------------------------------------------------------- Resumen
$resumen = New-Object System.Collections.Generic.List[string]
$resumen.Add("DATOS DE LA DEMO  ($(Get-Date -Format 'yyyy-MM-dd HH:mm'))")
$resumen.Add("Clave de todas las cuentas demo (excepto admin): $ClaveDemo")
$resumen.Add("")
$resumen.Add("RESTAURANTES")
$resumen.Add("  id $idSabor   Sabor 24")
$resumen.Add("  id $idParrilla   La Parrilla Dorada")
$resumen.Add("")
$resumen.Add("PLATOS (id | restaurante | categoria | precio | nombre)")
foreach ($p in $platos) { $resumen.Add(("  {0,3} | {1} | {2,-12} | {3,6} | {4}" -f $p.Id, $p.IdRestaurante, $p.Categoria, $p.Precio, $p.Nombre)) }
$resumen.Add("")
$resumen.Add("USUARIOS (rol | id | correo | detalle)")
$resumen.Add("  ADMINISTRADOR | -          | $AdminCorreo")
$resumen.Add("  PROPIETARIO   | $idPropietario | $($propietario.correo) | dueno de los 2 restaurantes")
$resumen.Add("  EMPLEADO      | $idEmpleado1 | $($empleado1.correo) | restaurante $idSabor (Sabor 24)")
$resumen.Add("  EMPLEADO      | $idEmpleado2 | $($empleado2.correo) | restaurante $idParrilla (La Parrilla Dorada)")
$resumen.Add("  CLIENTE       | $idCliente1 | $($cliente1.correo) | celular del SMS: $CelularCliente")
$resumen.Add("  CLIENTE       | $idCliente2 | $($cliente2.correo) | para probar la cancelacion de un pedido ajeno")
$resumen.Add("")
$resumen.Add("TOKENS (pegar en Authorize de Swagger, sin la palabra Bearer)")
$resumen.Add("ADMINISTRADOR:"); $resumen.Add($tokenAdmin); $resumen.Add("")
$resumen.Add("PROPIETARIO:");   $resumen.Add($tokenPropietario); $resumen.Add("")
$resumen.Add("EMPLEADO 1 (Sabor 24):"); $resumen.Add($tokenEmpleado1); $resumen.Add("")
$resumen.Add("EMPLEADO 2 (La Parrilla Dorada):"); $resumen.Add($tokenEmpleado2); $resumen.Add("")
$resumen.Add("CLIENTE 1:"); $resumen.Add($tokenCliente1); $resumen.Add("")
$resumen.Add("CLIENTE 2:"); $resumen.Add($tokenCliente2)

$archivo = Join-Path $PSScriptRoot "resumen-demo.local.txt"
[IO.File]::WriteAllLines($archivo, $resumen, (New-Object Text.UTF8Encoding $false))

Write-Host ""
Write-Host "LISTO. Resumen con ids y tokens en: $archivo" -ForegroundColor Green
Write-Host "Ese archivo NO se sube a git (tiene tokens)."
Write-Host ""
$resumen | Select-Object -First 27 | ForEach-Object { Write-Host $_ }