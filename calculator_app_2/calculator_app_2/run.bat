@echo off
echo 🛠️ Compiling Java files...
javac -cp ".;lib/sqlite-jdbc-3.36.0.3.jar" src/SwingCalculator.java -d bin

if %errorlevel% neq 0 (
    echo ❌ Compilation failed!
    pause
    exit /b
)

echo 🚀 Running Calculator...
java -cp ".;bin;lib/sqlite-jdbc-3.36.0.3.jar" SwingCalculator
pause

