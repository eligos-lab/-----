@echo off
where mvn >nul 2>&1
if errorlevel 1 (
    echo ❌ Maven не найден. Добавь Maven в PATH.
    pause
    exit /b
)

echo 🔄 Building and running Air Quality Tracker...
mvn clean package exec:java -Dexec.mainClass=org.example.Main
pause
