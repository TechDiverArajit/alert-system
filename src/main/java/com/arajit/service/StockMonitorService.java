package com.arajit.service;

import com.arajit.config.MonitorProperties;
import com.arajit.model.ProductInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class StockMonitorService {

    private final StockChecker stockChecker;
    private final TelegramService telegramService;
    private final MonitorProperties monitorProperties;
    private final Map<String, Set<String>> lastNotifiedProducts = new ConcurrentHashMap<>();
    private final Map<String, Long> lastCheckTime = new ConcurrentHashMap<>();

    public StockMonitorService(StockChecker stockChecker, TelegramService telegramService, 
                              MonitorProperties monitorProperties) {
        this.stockChecker = stockChecker;
        this.telegramService = telegramService;
        this.monitorProperties = monitorProperties;
        
        log.info("================================");
        log.info("🎮 PlayStation Stock Monitor");
        log.info("================================");
        log.info("⏱️  Check Interval: {} seconds", monitorProperties.getIntervalSeconds());
        log.info("🌐 Monitoring {} site(s)", monitorProperties.getSites().size());
        for (MonitorProperties.SiteConfig site : monitorProperties.getSites()) {
            log.info("   • {}: {}", site.getName(), site.getUrl());
        }
        log.info("================================\n");
    }

    @Scheduled(fixedDelayString = "${monitor.interval-seconds}000", initialDelay = 2000)
    public void monitorStock() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String timestamp = LocalDateTime.now().format(formatter);
        
        log.info("\n🔄 [{}] Starting stock check...", timestamp);

        for (MonitorProperties.SiteConfig site : monitorProperties.getSites()) {
            try {
                log.info("🌐 Checking: {}", site.getName());
                lastCheckTime.put(site.getName(), System.currentTimeMillis());

                List<ProductInfo> inStockProducts = stockChecker.checkStock(site.getUrl());

                if (!inStockProducts.isEmpty()) {
                    log.warn("⚠️  Found {} in-stock product(s) at {}", inStockProducts.size(), site.getName());
                    notifyNewProducts(site.getName(), inStockProducts, site.getUrl());
                } else {
                    log.info("❌ No in-stock products at {}", site.getName());
                }

            } catch (Exception e) {
                log.error("❌ Error checking {}: {}", site.getName(), e.getMessage());
                telegramService.sendDebugMessage("Error checking " + site.getName() + ": " + e.getMessage());
            }
        }

        log.info("✅ Check completed\n");
    }

    private void notifyNewProducts(String siteName, List<ProductInfo> products, String siteUrl) {
        Set<String> notified = lastNotifiedProducts.computeIfAbsent(siteName, k -> new HashSet<>());

        for (ProductInfo product : products) {
            String productKey = product.getName() + "|" + product.getPrice();

            if (!notified.contains(productKey)) {
                log.info("🚀 SENDING ALERT for: {} - {}", product.getName(), product.getPrice());
                
                String productInfo = String.format(
                        "%s\n💰 Price: %s\n📌 Indicator: %s",
                        product.getName(),
                        product.getPrice(),
                        product.getStockIndicator()
                );

                telegramService.sendStockAlert(siteName, productInfo, siteUrl);
                notified.add(productKey);
            }
        }
    }

    public Map<String, Object> getMonitorStatus() {
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("timestamp", LocalDateTime.now());
        status.put("intervalSeconds", monitorProperties.getIntervalSeconds());
        status.put("sites", new ArrayList<>());

        for (MonitorProperties.SiteConfig site : monitorProperties.getSites()) {
            Map<String, Object> siteStatus = new LinkedHashMap<>();
            siteStatus.put("name", site.getName());
            siteStatus.put("url", site.getUrl());
            siteStatus.put("lastCheckTime", lastCheckTime.get(site.getName()));
            siteStatus.put("notifiedProducts", lastNotifiedProducts.getOrDefault(site.getName(), new HashSet<>()).size());
            
            ((List<Map<String, Object>>) status.get("sites")).add(siteStatus);
        }

        return status;
    }
}
