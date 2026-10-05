@echo off
chcp 65001 > nul
java -Dfile.encoding=UTF-8 -Dconsole.encoding=UTF-8 -jar bchs-1.0-SNAPSHOT-jar-with-dependencies.jar
pause
