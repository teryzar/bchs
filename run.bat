@echo off
chcp 65001 > nul
java -Dfile.encoding=UTF-8 -Dconsole.encoding=UTF-8 -jar target\bchs-1.0-SNAPSHOT-jar-with-dependencies.jar
pause
