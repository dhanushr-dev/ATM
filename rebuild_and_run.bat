@echo off
echo Rebuilding JAR with updated database.properties...
call mvn package -DskipTests -q
if %errorlevel% neq 0 (
    echo Build failed!
    pause
    exit /b 1
)
echo Build successful. Launching ATM Interface...
java -jar "target\ATM-Interface.jar"
