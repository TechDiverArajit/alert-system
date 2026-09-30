# Quick Start Guide 🚀

Get your PlayStation Stock Monitor running in 5 minutes!

## Step 1: Prepare Telegram (2 minutes)

### Create a Telegram Bot

1. Open Telegram and search for **@BotFather**
2. Send `/newbot`
3. Choose a name (e.g., "PS Stock Monitor")
4. Choose a username (e.g., "ps_stock_monitor_bot")
5. **Copy the bot token** - save it somewhere (looks like: `123456789:ABCdefGHIjklmNOpqrSTUvwxyz`)

### Get Your Chat ID

1. Open the bot you just created
2. Send any message (e.g., "hi")
3. **In your browser**, visit:
   ```
   https://api.telegram.org/bot<YOUR_BOT_TOKEN>/getUpdates
   ```
4. Look for the response and find:
   ```json
   "chat": {
     "id": 123456789,
     ...
   }
   ```
5. **Copy that chat ID number** (just the number, e.g., `123456789`)

---

## Step 2: Update Configuration (1 minute)

Open `src/main/resources/application.yml` and replace:

```yaml
monitor:
  interval-seconds: 10
  telegram:
    bot-token: 8397115513:AAFhkJ5gLT9gMuGtk6NpbQzfGspYkA5KWJI  # ← Already filled!
    chat-id: YOUR_CHAT_ID_HERE                                # ← Replace with your chat ID
  sites:
    - name: "JioGames PlayStation"
      url: "https://store.jiogames.com/products/?brand=playstation"
      check-type: "PLAYWRIGHT"
```

**Example after filling in:**
```yaml
monitor:
  interval-seconds: 10
  telegram:
    bot-token: 8397115513:AAFhkJ5gLT9gMuGtk6NpbQzfGspYkA5KWJI
    chat-id: 123456789
  sites:
    - name: "JioGames PlayStation"
      url: "https://store.jiogames.com/products/?brand=playstation"
      check-type: "PLAYWRIGHT"
```

**Save the file.**

---

## Step 3: Setup (1 minute)

### Windows Users
```bash
setup.bat
```

### Linux/Mac Users
```bash
chmod +x setup.sh
./setup.sh
```

This installs the Playwright browser automatically.

---

## Step 4: Run the Monitor (1 minute)

```bash
mvn spring-boot:run
```

### Expected Output:

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

🔄 [2024-01-15 14:32:20] Starting stock check...
🌐 Checking: JioGames PlayStation
❌ No in-stock products at JioGames PlayStation
✅ Check completed
```

---

## Step 5: Test Your Telegram Notification

The monitor is now running and checking every 10 seconds. You should receive a Telegram message when stock is found.

### Manual Test (Optional)

If you want to test the Telegram integration without waiting for stock:

1. Open a new terminal in the project directory
2. Run this curl command:
   ```bash
   curl -X GET http://localhost:8080/api/monitor/health
   ```
3. You should see: `{"status":"UP","message":"PlayStation Stock Monitor is running"}`
4. Check your monitor status:
   ```bash
   curl -X GET http://localhost:8080/api/monitor/status
   ```

---

## What to Expect

### When Stock is NOT Available
```
❌ No in-stock products at JioGames PlayStation
```
(You won't get a notification)

### When Stock IS Available
1. **Console shows:**
   ```
   ✅ IN STOCK: PlayStation Network Gift Card - ₹1000
   🚀 SENDING ALERT for: PlayStation Network Gift Card - ₹1000
   ```

2. **You get a Telegram message:**
   ```
   🎮 PLAYSTATION GIFT CARD IN STOCK!

   Site: JioGames PlayStation
   Product: PlayStation Network Gift Card - ₹1000
   Time: 15/01/2024 14:35:22
   Link: https://store.jiogames.com/products/?brand=playstation
   ```

---

## Troubleshooting

### Issue: "Playwright browser initialization failed"
**Solution:**
```bash
mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="install chromium"
```

### Issue: "Telegram API error"
**Solution:** Check your bot token and chat ID:
```bash
# Test bot token
curl https://api.telegram.org/bot<YOUR_BOT_TOKEN>/getMe

# Check for messages
curl https://api.telegram.org/bot<YOUR_BOT_TOKEN>/getUpdates
```

### Issue: "No product cards found"
**Reason:** Website layout changed
**Solution:** The code will automatically try alternative selectors. If it still fails, you can update the selectors in `StockChecker.java` lines where product cards are searched.

### Issue: "Port 8080 already in use"
**Solution:** Change the port in `application.yml`:
```yaml
server:
  port: 8081  # Use a different port
```

---

## Keep It Running 24/7

### Option 1: Run on Your PC
Keep the terminal window open. The monitor runs continuously.

### Option 2: Use a VPS (Recommended)
```bash
nohup mvn spring-boot:run &
```
This runs in the background.

### Option 3: Use Docker
Create a `Dockerfile` to containerize the application (optional).

### Option 4: Windows Task Scheduler
Create a batch file that runs the Java command and schedule it to start at boot.

---

## Next Steps

Once running:
1. **Monitor the logs** - watch for "IN STOCK" messages
2. **Keep it running** - leave it on for 24+ hours for best results
3. **Adjust interval** - if you want faster checks, change `interval-seconds: 10` to `5` (but be respectful to the website)
4. **Add more sites** - add more URLs to `sites:` in `application.yml`

---

## Still Need Help?

Check the full documentation in `README.md` for:
- More detailed setup
- API endpoint details
- Troubleshooting
- Adding multiple sites
- Performance tuning

**Happy hunting! 🎮**
