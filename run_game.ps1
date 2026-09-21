# PowerShell launcher script for Flow FXML JavaFX
Write-Host "========================================================" -ForegroundColor Cyan
Write-Host " FLOW: FLOODED IUT CAMPUS RESCUE MISSION - JAVAFX" -ForegroundColor Yellow
Write-Host "========================================================" -ForegroundColor Cyan

$projDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $projDir

if (-not (Test-Path bin)) { New-Item -ItemType Directory -Force -Path bin }

Write-Host "Compiling Flow FXML JavaFX source code..." -ForegroundColor Gray
javac --module-path lib --add-modules javafx.controls,javafx.fxml,javafx.media -d bin src/com/flow/*.java src/com/flow/controller/*.java src/com/flow/game/*.java src/com/flow/model/*.java src/com/flow/view/*.java

if ($LASTEXITCODE -eq 0) {
    if (-not (Test-Path bin/com/flow/view)) { New-Item -ItemType Directory -Force -Path bin/com/flow/view }
    Copy-Item src/com/flow/view/GameView.fxml bin/com/flow/view/GameView.fxml -Force
    Copy-Item src/com/flow/view/menu.css bin/com/flow/view/menu.css -Force

    if (-not (Test-Path bin/Resources/Images)) { New-Item -ItemType Directory -Force -Path bin/Resources/Images }
    Copy-Item src/Resources/Images/* bin/Resources/Images/ -Force -Recurse

    Write-Host "Compilation successful! Starting Flow game..." -ForegroundColor Green
    java --module-path lib --add-modules javafx.controls,javafx.fxml,javafx.media -cp bin com.flow.MainLauncher
} else {
    Write-Host "Compilation failed!" -ForegroundColor Red
}
