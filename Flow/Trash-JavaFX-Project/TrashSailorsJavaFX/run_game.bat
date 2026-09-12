@echo off
echo ========================================================
echo  TRASH SAILORS: SEAWARD CO-OP - FXML JAVAFX GAME
echo ========================================================
echo Compiling FXML source files...
if not exist bin mkdir bin
javac --module-path lib --add-modules javafx.controls,javafx.fxml,javafx.media -d bin src/com/trashsailors/*.java src/com/trashsailors/controller/*.java src/com/trashsailors/game/*.java src/com/trashsailors/model/*.java src/com/trashsailors/view/*.java

if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Compilation failed!
    pause
    exit /b %ERRORLEVEL%
)

if not exist bin\com\trashsailors\view mkdir bin\com\trashsailors\view
copy /Y src\com\trashsailors\view\GameView.fxml bin\com\trashsailors\view\GameView.fxml

echo Compilation successful! Launching FXML Game...
java --module-path lib --add-modules javafx.controls,javafx.fxml,javafx.media -cp bin com.trashsailors.MainLauncher
pause
