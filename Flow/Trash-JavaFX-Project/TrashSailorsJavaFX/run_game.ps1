# PowerShell launcher script for Trash Sailors FXML JavaFX
Write-Host "========================================================" -ForegroundColor Cyan
Write-Host " TRASH SAILORS: SEAWARD CO-OP - FXML JAVAFX GAME" -ForegroundColor Yellow
Write-Host "========================================================" -ForegroundColor Cyan

$projDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $projDir

if (-not (Test-Path bin)) { New-Item -ItemType Directory -Force -Path bin }

Write-Host "Compiling FXML JavaFX source code..." -ForegroundColor Gray
javac --module-path lib --add-modules javafx.controls,javafx.fxml,javafx.media -d bin src/com/trashsailors/*.java src/com/trashsailors/controller/*.java src/com/trashsailors/game/*.java src/com/trashsailors/model/*.java src/com/trashsailors/view/*.java

if ($LASTEXITCODE -eq 0) {
    if (-not (Test-Path bin/com/trashsailors/view)) { New-Item -ItemType Directory -Force -Path bin/com/trashsailors/view }
    Copy-Item src/com/trashsailors/view/GameView.fxml bin/com/trashsailors/view/GameView.fxml -Force
    Write-Host "Compilation successful! Starting game..." -ForegroundColor Green
    java --module-path lib --add-modules javafx.controls,javafx.fxml,javafx.media -cp bin com.trashsailors.MainLauncher
} else {
    Write-Host "Compilation failed!" -ForegroundColor Red
}
