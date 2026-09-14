@echo off
cd /d "%~dp0"

set TOKEN_FILE=telegram_token.txt

if exist "%TOKEN_FILE%" (
    set /p BOT_TOKEN=<"%TOKEN_FILE%"
)

if "%BOT_TOKEN%"=="" (
    set /p BOT_TOKEN=Inserisci token Telegram: 
)

java -jar bot.jar "%BOT_TOKEN%" 127.0.0.1 8080
pause
