#!/bin/bash

# ===========================================
#    КОМПИЛЯЦИЯ ПРОЕКТА ТАКСИ (Linux/macOS)
# ===========================================

echo "==========================================="
echo "   COMPILATION OF TAXI PROJECT"
echo "==========================================="

# Создаем папку bin, если ее нет
if [ ! -d "bin" ]; then
    mkdir bin
fi

# Компилируем все java файлы
echo "Compilation of Java files..."

# Компилируем основные классы
javac -d bin \
    -cp "src" \
    src/*.java \
    src/infra/*.java \
    src/models/*.java \
    src/services/*.java \
    src/stats/*.java \
    src/util/*.java

# Проверяем успешность компиляции
if [ $? -eq 0 ]; then
    echo "==========================================="
    echo "   COMPILATION FINISHED SUCCESSFULLY"
    echo "   Files are compiled into bin/"
    echo "==========================================="
else
    echo "==========================================="
    echo "   COMPILATION ERROR!"
    echo "==========================================="
    exit 1
fi
