# Script para generar release.keystore y copiar Base64 al portapapeles
$keystorePath = Join-Path $PSScriptRoot "..\release.keystore"
$keytoolPath = "C:\Program Files\Android\Android Studio\jbr\bin\keytool.exe"

$alias = "samiboxtv"
$password = "SamiBox2026*"

Write-Host "Generando release.keystore..." -ForegroundColor Cyan

if (Test-Path $keystorePath) {
    Write-Host "El archivo release.keystore ya existe en: $keystorePath" -ForegroundColor Yellow
} else {
    & $keytoolPath -genkeypair -v `
        -keystore $keystorePath `
        -alias $alias `
        -keyalg RSA `
        -keysize 2048 `
        -validity 10000 `
        -storepass $password `
        -keypass $password `
        -dname "CN=SamiBox TV, OU=Mobile, O=SamiBox, L=Bogota, ST=Cundinamarca, C=CO"

    if (Test-Path $keystorePath) {
        Write-Host "Keystore generado exitosamente." -ForegroundColor Green
    } else {
        Write-Host "Error al generar el keystore." -ForegroundColor Red
        exit 1
    }
}

# Convertir a Base64 y copiar al portapapeles
$base64 = [Convert]::ToBase64String([IO.File]::ReadAllBytes($keystorePath))
$base64 | Set-Clipboard

Write-Host "`n=======================================================" -ForegroundColor Green
Write-Host "¡Base64 copiado al portapapeles con éxito!" -ForegroundColor Green
Write-Host "=======================================================" -ForegroundColor Green
Write-Host "Configura los siguientes valores en GitHub Secrets:" -ForegroundColor Cyan
Write-Host "1. KEYSTORE_BASE64   -> (Ya está en tu portapapeles, solo pulsa Ctrl+V)" -ForegroundColor White
Write-Host "2. KEYSTORE_PASSWORD -> $password" -ForegroundColor White
Write-Host "3. KEY_ALIAS         -> $alias" -ForegroundColor White
Write-Host "4. KEY_PASSWORD      -> $password" -ForegroundColor White
Write-Host "=======================================================`n" -ForegroundColor Green
