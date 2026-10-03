<#
  Carga los datos semilla de pruebas (admin, médicos, agenda) en MySQL.
  Requisito: citas-api debe haber arrancado al menos una vez (Flyway crea esquema y catálogos).

  Uso (MySQL en Docker):
    .\scripts\seed-db.ps1 -Container fcv-citas-mysql -RootPassword "<root>" -Database citas_fcv_training

  Uso (MySQL instalado localmente, sin Docker):
    mysql -u root -p --default-character-set=utf8mb4 citas_fcv_training < database/seed/seed-test-data.sql
#>
param(
  [string]$Container = "fcv-citas-mysql",
  [string]$Database = "citas_fcv_training",
  [Parameter(Mandatory = $true)][string]$RootPassword
)
$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $PSScriptRoot
$sqlFile = Join-Path $Root "database/seed/seed-test-data.sql"

$check = docker exec -e MYSQL_PWD=$RootPassword $Container mysql -uroot -N -B -e "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='$Database' AND table_name='professionals';" 2>$null
if ("$check".Trim() -ne "1") {
  throw "El esquema aún no existe. Arranca citas-api una vez (Flyway lo crea) y vuelve a ejecutar este script."
}

# Se copia al contenedor para preservar UTF-8 (tildes y ñ).
docker cp $sqlFile "${Container}:/tmp/seed-test-data.sql"
docker exec -e MYSQL_PWD=$RootPassword $Container sh -c "mysql -uroot --default-character-set=utf8mb4 $Database < /tmp/seed-test-data.sql"
if ($LASTEXITCODE -ne 0) { throw "La carga de datos semilla falló" }
Write-Host "[OK] Datos semilla cargados. Cuentas: admin1@medih.com, medico1..24@medih.com (clave: medih123)" -ForegroundColor Green
