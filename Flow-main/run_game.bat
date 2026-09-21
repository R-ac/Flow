@echo off
echo ========================================================
echo  FLOW: FLOODED IUT CAMPUS RESCUE MISSION - JAVAFX
echo ========================================================
echo Compiling FXML source files...
if not exist bin mkdir bin
javac --module-path lib --add-modules javafx.controls,javafx.fxml,javafx.media -d bin src/com/flow/*.java src/com/flow/controller/*.java src/com/flow/game/*.java src/com/flow/model/*.java src/com/flow/view/*.java

if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Compilation failed!
    pause
    exit /b %ERRORLEVEL%
)

if not exist bin\com\flow\view mkdir bin\com\flow\view
copy /Y src\com\flow\view\GameView.fxml bin\com\flow\view\GameView.fxml
copy /Y src\com\flow\view\menu.css bin\com\flow\view\menu.css

if not exist bin\Resources\Images mkdir bin\Resources\Images
xcopy /Y /S /I src\Resources\Images bin\Resources\Images >nul

echo Compilation successful! Launching Flow Game...
java --module-path lib --add-modules javafx.controls,javafx.fxml,javafx.media -cp bin com.flow.MainLauncher
pause
