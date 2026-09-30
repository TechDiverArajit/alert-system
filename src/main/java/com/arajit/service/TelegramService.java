package com.arajit.service;

import com.arajit.config.MonitorProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class TelegramService {

    private final MonitorProperties monitorProperties;
    private final RestClient restClient;
    private static final String TELEGRAM_API_URL = "https://api.telegram.org/bot";

    public TelegramService(MonitorProperties monitorProperties, RestClient restClient) {
        this.monitorProperties = monitorProperties;
        this.restClient = restClient;
    }

    public void sendStockAlert(String siteName, String productInfo, String productUrl) {
        try {
            String message = formatAlertMessage(siteName, productInfo, productUrl);
            sendMessage(message);
            log.info("✅ Telegram notification sent for: {}", siteName);
        } catch (Exception e) {
            log.error("❌ Failed to send Telegram notification: {}", e.getMessage(), e);
        }
    }

    public void sendDebugMessage(String message) {
        try {
            sendMessage("🔍 DEBUG: " + message);
        } catch (Exception e) {
            log.error("Failed to send debug message: {}", e.getMessage());
        }
    }

    private void sendMessage(String text) {
        String botToken = monitorProperties.getTelegram().getBotToken();
        String chatId = monitorProperties.getTelegram().getChatId();
        String url = TELEGRAM_API_URL + botToken + "/sendMessage";

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("chat_id", chatId);
        requestBody.put("text", text);
        requestBody.put("parse_mode", "HTML");

        try {
            restClient.post()
                    .uri(url)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);
            log.debug("Message sent to Telegram");
        } catch (RestClientException e) {
            log.error("Telegram API error: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to send Telegram message", e);
        }
    }

    private String formatAlertMessage(String siteName, String productInfo, String productUrl) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        String timestamp = LocalDateTime.now().format(formatter);

        return String.format(
                "<b>🎮 PLAYSTATION GIFT CARD IN STOCK!</b>\n\n" +
                "<b>Site:</b> %s\n" +
                "<b>Product:</b> %s\n" +
                "<b>Time:</b> %s\n" +
                "<b>Link:</b> <a href=\"%s\">Click to Buy</a>",
                siteName, productInfo, timestamp, productUrl
        );
    }
}
