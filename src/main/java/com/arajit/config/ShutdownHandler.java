package com.arajit.config;

import com.arajit.service.StockChecker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ShutdownHandler {

    private final StockChecker stockChecker;

    public ShutdownHandler(StockChecker stockChecker) {
        this.stockChecker = stockChecker;
    }

    @EventListener(ContextClosedEvent.class)
    public void onShutdown() {
        log.info("🛑 Shutting down application...");
        stockChecker.shutdown();
        log.info("✅ Application shutdown complete");
    }
}
