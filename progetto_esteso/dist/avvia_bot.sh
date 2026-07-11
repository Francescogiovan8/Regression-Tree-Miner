#!/bin/bash
cd "$(dirname "$0")"

TOKEN_FILE="telegram_token.txt"

if [ -f "$TOKEN_FILE" ]; then
	BOT_TOKEN="$(head -n 1 "$TOKEN_FILE" | tr -d '\r\n')"
fi

if [ -z "$BOT_TOKEN" ]; then
	read -p "Inserisci token Telegram: " BOT_TOKEN
fi

java -jar bot.jar "$BOT_TOKEN" 127.0.0.1 8080
