#!/bin/bash

echo "🔍 Checking environment..."

# Проверка Java
if ! command -v java >/dev/null 2>&1; then
  echo "❌ Java не найден. Установи JDK 11+ и добавь java в PATH."
  exit 1
fi

# Проверка Maven
if ! command -v mvn >/dev/null 2>&1; then
  echo "❌ Maven не найден. Установи Maven и добавь mvn в PATH."
  exit 1
fi

echo "✅ Java and Maven found"
echo "🔄 Building and running Air Quality Tracker..."

mvn clean package exec:java -Dexec.mainClass=org.example.Main
