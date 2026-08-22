@echo off
echo ============================================================
echo  ATM Interface - Database Setup
echo  Oasis Infobyte Java Development Internship
echo ============================================================
echo.

set /p MYSQL_PASS=Enter your MySQL root password: 

echo.
echo [1/2] Creating database schema...
mysql -u root -p%MYSQL_PASS% < "src\main\resources\sql\schema.sql"
if %errorlevel% neq 0 (
    echo ERROR: Schema creation failed. Check your password and ensure MySQL is running.
    pause
    exit /b 1
)
echo     Schema created successfully.

echo.
echo [2/2] Loading sample data...
mysql -u root -p%MYSQL_PASS% < "src\main\resources\sql\sample_data.sql"
if %errorlevel% neq 0 (
    echo ERROR: Sample data loading failed.
    pause
    exit /b 1
)
echo     Sample data loaded successfully.

echo.
echo [3/3] Updating database.properties with your password...
powershell -Command "(Get-Content 'src\main\resources\database.properties') -replace 'db.password=.*', 'db.password=%MYSQL_PASS%' | Set-Content 'src\main\resources\database.properties'"
echo     database.properties updated.

echo.
echo ============================================================
echo  Setup complete! 
echo.
echo  Test credentials:
echo    Account: 1001000000000001   PIN: 1234
echo    Account: 1001000000000002   PIN: 5678
echo    Account: 1001000000000003   PIN: 9999
echo ============================================================
echo.
pause
