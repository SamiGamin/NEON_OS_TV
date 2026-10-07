param(
    [Parameter(Mandatory=$false)]
    [string]$Version = ""
)

if (-not $Version) {
    $defaultVersion = "v1.0.0"
    $Version = Read-Host "Ingresa el nombre del tag de la versión (por defecto $defaultVersion)"
    if (-not $Version) { $Version = $defaultVersion }
}

# Asegurar que empieza con 'v'
if (-not $Version.StartsWith("v")) {
    $Version = "v$Version"
}

Write-Host "Verificando estado de Git..." -ForegroundColor Cyan
$gitStatus = git status --porcelain
if ($gitStatus) {
    Write-Host "Hay cambios pendientes sin commitear. Por favor haz commit antes de crear el tag." -ForegroundColor Yellow
    exit 1
}

Write-Host "Creando tag $Version..." -ForegroundColor Green
git tag -a $Version -m "Release $Version"

Write-Host "Enviando tag a GitHub ($Version)..." -ForegroundColor Green
git push origin $Version

Write-Host "¡Tag enviado exitosamente!" -ForegroundColor Green
Write-Host "GitHub Actions iniciará automáticamente la compilación y publicación de la Release." -ForegroundColor Cyan
