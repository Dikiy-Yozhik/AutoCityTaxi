@echo off
echo ============================================
echo    COMPILATION OF TAXI PROJECT
echo ============================================

REM Создаем папку bin, если ее нет
if not exist "bin" mkdir bin

REM Компилируем все java файлы в папке bin
echo Compilation of Java files...
javac -d bin -cp "src" src/*.java src/infra/*.java src/models/*.java src/services/*.java src/stats/*.java src/util/*.java

if %errorlevel% neq 0 (
    echo COMPILATION ERROR!
    pause
    exit /b 1
)

echo ============================================
echo    COMPILATION FINISHED SUCCESSFULLY
echo    Files are compiled into bin/
echo ============================================
pause