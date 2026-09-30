package com.arajit.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "monitor")
@Data
public class MonitorProperties {
    private int intervalSeconds;
    private TelegramConfig telegram;
    private List<SiteConfig> sites;

    @Data
    public static class TelegramConfig {
        private String botToken;
        private String chatId;
    }

    @Data
    public static class SiteConfig {
        private String name;
        private String url;
        private String checkType;
    }
}
