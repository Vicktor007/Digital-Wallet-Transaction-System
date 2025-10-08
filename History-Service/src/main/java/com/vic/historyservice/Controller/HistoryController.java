package com.vic.historyservice.Controller;

import com.vic.historyservice.Models.Transaction_events;
import com.vic.historyservice.Service.HistoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("/api")
public class HistoryController {

    private final HistoryService historyService;

    public HistoryController(HistoryService historyService) {
        this.historyService = historyService;
    }

    @GetMapping("/wallets/{walletId}/history")
    public ResponseEntity<List<Transaction_events>> getWalletHistory(@PathVariable String walletId) {
        return ResponseEntity.ok(historyService.getWalletHistory(walletId));
    }

    @GetMapping("/users/{userId}/activity")
    public ResponseEntity<List<Transaction_events>> getUserActivityHistory(@PathVariable String userId) {
        return ResponseEntity.ok(historyService.getUserHistory(userId));
    }
}
