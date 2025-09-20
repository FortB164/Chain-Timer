REM Copy JavaFX JARs to target
xcopy /Y /S "C:\path\to\javafx-sdk-21\lib\*.jar" target\

jpackage ^
  --name ChainTimer ^
  --input target ^
  --main-jar chain-timer-1.0.0.jar ^
  --main-class com.timer.Main ^
  --type app-image ^
  --java-options "--add-opens=javafx.graphics/javafx.stage=ALL-UNNAMED"

REM Copy JavaFX DLLs to the app image bin directory
xcopy /Y /S "C:\path\to\javafx-sdk-21\bin\*.dll" "ChainTimer\app\bin\"