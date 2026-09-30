# PlayStation Stock Monitor 🎮

A real-time Spring Boot application that monitors JioGames for PlayStation gift card stock availability and sends instant Telegram notifications.

## Features

✅ **Real-time Monitoring** - Checks stock every 10 seconds  
✅ **Telegram Notifications** - Instant alerts with product details and direct links  
✅ **Smart Deduplication** - Avoids notification spam for same products  
✅ **Playwright-based Detection** - Handles JavaScript-rendered pages  
✅ **REST API** - Check monitor status via HTTP  
✅ **Production Ready** - Error handling, logging, and graceful shutdown

## Prerequisites

- **Java 17+** installed
- **Maven 3.8+** installed
- **Telegram Bot Token** - Get from [@BotFather](https://t.me/botfather)
- **Your Telegram Chat ID** - Message your bot and get the ID from API response

## Setup Instructions

### Step 1: Get Telegram Bot Token and Chat ID

1. Message [@BotFather](https://t.me/botfather) on Telegram
2. Send `/newbot` and follow the prompts to create a new bot
3. Copy the bot token (provided by BotFather)
4. Message your new bot with `/start` or any text
5. Visit: `https://api.telegram.org/bot<YOUR_BOT_TOKEN>/getUpdates`
6. Look for the `chat` → `id` field in the JSON response

### Step 2: Update Configuration

Edit `src/main/resources/application.yml`:

```yaml
monitor:
  interval-seconds: 10  # Check every 10 seconds
  telegram:
    bot-token: YOUR_BOT_TOKEN_HERE
    chat-id: YOUR_CHAT_ID_HERE
  sites:
    - name: "JioGames PlayStation"
      url: "https://store.jiogames.com/products/?brand=playstation"
      check-type: "PLAYWRIGHT"
```

### Step 3: Install Playwright Browsers

Before running, install Chromium:

```bash
mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="install chromium"
```

### Step 4: Build and Run

```bash
# Build the project
mvn clean package

# Run the application
mvn spring-boot:run

# Or run the JAR directly
java -jar target/playstation-stock-monitor-1.0.0.jar
```

**Expected Output:**
```
================================
🎮 PlayStation Stock Monitor
================================
⏱️  Check Interval: 10 seconds
🌐 Monitoring 1 site(s)
   • JioGames PlayStation: https://store.jiogames.com/products/?brand=playstation
================================

✅ Playwright browser initialized
🔄 [2024-01-15 14:32:10] Starting stock check...
🌐 Checking: JioGames PlayStation
❌ No in-stock products at JioGames PlayStation
✅ Check completed
```

## API Endpoints

### Check Monitor Status
```bash
curl http://localhost:8080/api/monitor/status
```

**Response:**
```json
{
  "timestamp": "2024-01-15T14:35:22.123",
  "intervalSeconds": 10,
  "sites": [
    {
      "name": "JioGames PlayStation",
      "url": "https://store.jiogames.com/products/?brand=playstation",
      "lastCheckTime": 1705329322123,
      "notifiedProducts": 0
    }
  ]
}
```

### Health Check
```bash
curl http://localhost:8080/api/monitor/health
```

**Response:**
```json
{
  "status": "UP",
  "message": "PlayStation Stock Monitor is running"
}
```

## How It Works

1. **Scheduler** runs every 10 seconds
2. **Stock Checker** uses Playwright to:
   - Navigate to JioGames PlayStation products page
   - Wait for page to fully load (JavaScript rendered)
   - Parse product cards looking for:
     - "Add to Cart" / "Buy Now" buttons (not disabled)
     - "In Stock" text indicators
     - Absence of "Out of Stock" / "Sold Out" text
3. **State Tracker** compares results with previous check
4. **Telegram Service** sends alerts only for new in-stock products
5. **Logging** tracks all activity with timestamps

## Telegram Alert Format

When stock is found, you receive:
```
🎮 PLAYSTATION GIFT CARD IN STOCK!

Site: JioGames PlayStation
Product: PlayStation Network Gift Card - ₹1000
Time: 15/01/2024 14:35:22
Link: https://store.jiogames.com/products/?brand=playstation
```

## Logging

Logs show real-time status:
- ✅ In-stock products found
- ❌ No stock available
- 🔍 Product parsing details
- ⚠️ Errors and retries

Output is in `logs/` directory if configured.

## Troubleshooting

### No products detected
- Page structure may have changed - check the selectors in `StockChecker.java`
- Increase timeout in `Page.navigate()` to 60000ms
- Check browser console for JavaScript errors

### Telegram messages not sending
- Verify bot token and chat ID in `application.yml`
- Test token: `curl https://api.telegram.org/bot<TOKEN>/getMe`
- Test chat ID: `curl https://api.telegram.org/bot<TOKEN>/getUpdates`

### Playwright crashes
- Ensure Chromium is installed: `mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="install chromium"`
- Check Java version: `java -version` (should be 17+)

### High CPU usage
- Playwright uses a headless browser which is resource-intensive
- 10-second intervals on a modest machine should be fine
- If needed, increase interval to 30 seconds in `application.yml`

## Adding More Sites

To monitor additional sites, add entries to `application.yml`:

```yaml
sites:
  - name: "JioGames PlayStation"
    url: "https://store.jiogames.com/products/?brand=playstation"
    check-type: "PLAYWRIGHT"
  - name: "Flipkart PlayStation"
    url: "https://www.flipkart.com/..."
    check-type: "PLAYWRIGHT"
```

## Performance Tips

1. **Increase interval** if you see high CPU/memory usage
2. **Use API endpoints** instead of HTML parsing when available
3. **Add error alerts** to know when checks are failing
4. **Monitor logs** for parsing issues before they cause crashes

## Project Structure

```
playstation-stock-monitor/
├── pom.xml
├── README.md
└── src/main/java/com/arajit/
    ├── Application.java
    ├── config/
    │   ├── MonitorProperties.java
    │   ├── RestClientConfig.java
    │   └── ShutdownHandler.java
    ├── controller/
    │   └── MonitorController.java
    ├── model/
    │   └── ProductInfo.java
    └── service/
        ├── StockChecker.java
        ├── StockMonitorService.java
        └── TelegramService.java
```

## License

This project is created for personal use.

---

**Happy hunting for PlayStation gift cards! 🎮**

For issues or improvements, check the logs first and adjust selectors in `StockChecker.java` as needed.
