#!/bin/bash

# PlayStation Stock Monitor Setup Script for Linux/Mac

echo ""
echo "================================"
echo "PlayStation Stock Monitor Setup"
echo "================================"
echo ""

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "ERROR: Maven is not installed or not in PATH"
    echo "Please install Maven from https://maven.apache.org/download.cgi"
    exit 1
fi

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "ERROR: Java is not installed or not in PATH"
    echo "Please install Java 17+ from https://adoptium.net/"
    exit 1
fi

echo "✓ Java and Maven are installed"
echo ""

# Install Playwright browsers
echo "Installing Playwright browsers (this may take a few minutes)..."
mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="install chromium"

if [ $? -ne 0 ]; then
    echo "ERROR: Failed to install Playwright browsers"
    exit 1
fi

echo ""
echo "✓ Playwright installed successfully"
echo ""
echo "================================"
echo "Setup Complete!"
echo "================================"
echo ""
echo "Next steps:"
echo "1. Edit src/main/resources/application.yml"
echo "2. Add your Telegram bot token"
echo "3. Add your Telegram chat ID"
echo "4. Run: mvn spring-boot:run"
echo ""
echo "To get your Telegram Chat ID:"
echo "1. Message @BotFather on Telegram to create a bot"
echo "2. Copy the bot token"
echo "3. Message your bot"
echo "4. Visit: https://api.telegram.org/bot[TOKEN]/getUpdates"
echo "5. Find your chat ID in the JSON response"
echo ""
