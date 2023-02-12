@echo off
:: Game Runner
cd ./runner-publish/
start "" dotnet GameRunner.dll
:: Game Engine
cd ../engine-publish/
timeout /t 1
start "" dotnet Engine.dll
:: Game Logger
cd ../logger-publish/
timeout /t 1
start "" dotnet Logger.dll
:: Bots
timeout /t 3
cd ../reference-bot-publish/
timeout /t 3
:: Salah satu ReferenceBot nanti diganti ke java -jar path (path-nya disesuaikan dengan direktori bot yang telah di-build) untuk diuji coba
start java -jar ../starter-bots/JavaBot/target/JavaBot.jar
timeout /t 3
start "" dotnet ReferenceBot.dll
timeout /t 3
start "" dotnet ReferenceBot.dll
timeout /t 3
start "" dotnet ReferenceBot.dll
cd ../
pause