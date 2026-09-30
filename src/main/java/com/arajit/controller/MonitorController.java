package com.arajit.controller;

import com.arajit.service.StockMonitorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/monitor")
public class MonitorController {

    private final StockMonitorService stockMonitorService;

    public MonitorController(StockMonitorService stockMonitorService) {
        this.stockMonitorService = stockMonitorService;
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        return ResponseEntity.ok(stockMonitorService.getMonitorStatus());
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("message", "PlayStation Stock Monitor is running");
        return ResponseEntity.ok(response);
    }
}
