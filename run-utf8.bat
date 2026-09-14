@echo off
REM Переключаємо консоль в UTF-8
chcp 65001 > nul

REM Встановлюємо UTF-8 для Java
set JAVA_TOOL_OPTIONS=-Dfile.encoding=UTF-8

REM Запускаємо програму
java -jar target\bchs-1.0-SNAPSHOT-jar-with-dependencies.jar

pause
