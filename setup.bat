@echo off
REM PlayStation Stock Monitor Setup Script for Windows

echo.
echo ================================
echo PlayStation Stock Monitor Setup
echo ================================
echo.

REM Check if Maven is installed
where mvn >nul 2>nul
if %errorlevel% neq 0 (
    echo ERROR: Maven is not installed or not in PATH
    echo Please install Maven from https://maven.apache.org/download.cgi
    pause
    exit /b 1
)

REM Check if Java is installed
where java >nul 2>nul
if %errorlevel% neq 0 (
    echo ERROR: Java is not installed or not in PATH
    echo Please install Java 17+ from https://adoptium.net/
    pause
    exit /b 1
)

echo ✓ Java and Maven are installed
echo.

REM Install Playwright browsers
echo Installing Playwright browsers...
mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="install chromium"

if %errorlevel% neq 0 (
    echo ERROR: Failed to install Playwright browsers
    pause
    exit /b 1
)

echo.
echo ✓ Playwright installed successfully
echo.
echo ================================
echo Setup Complete!
echo ================================
echo.
echo Next steps:
echo 1. Edit src/main/resources/application.yml
echo 2. Add your Telegram bot token
echo 3. Add your Telegram chat ID
echo 4. Run: mvn spring-boot:run
echo.
echo To get your Telegram Chat ID:
echo 1. Message @BotFather on Telegram to create a bot
echo 2. Copy the bot token
echo 3. Message your bot
echo 4. Visit: https://api.telegram.org/bot[TOKEN]/getUpdates
echo 5. Find your chat ID in the JSON response
echo.
pause
