package com.vic.walletservice.Controller;

import com.vic.walletservice.Dtos.CreateWalletRequest;
import com.vic.walletservice.Dtos.FundWalletRequest;
import com.vic.walletservice.Dtos.TransferRequest;
import com.vic.walletservice.Dtos.WalletResponse;
import com.vic.walletservice.Enums.TransactionStatus;
import com.vic.walletservice.Models.Wallet;
import com.vic.walletservice.Services.walletService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController("/api")
public class WalletController {

    private final walletService walletService;

    public WalletController(walletService walletService) {
        this.walletService = walletService;
    }

    @PostMapping("/wallets")
    public ResponseEntity<WalletResponse> createWallet(@RequestBody CreateWalletRequest wallet) {
        WalletResponse walletResponse = walletService.createWallet(wallet);
        return ResponseEntity.ok(walletResponse);
    }

    @PostMapping("/wallets/{walletId}/fund")
    public ResponseEntity<TransactionStatus> fundWallet(
            @PathVariable String walletId,
            @RequestBody FundWalletRequest fundWalletRequest
    ) {
        TransactionStatus status = walletService.fundWallet(walletId, fundWalletRequest.userId(), fundWalletRequest.amount());
        return ResponseEntity.ok(status);
    }

    @PostMapping("/wallets/{walletId}/transfer")
    public ResponseEntity<TransactionStatus> transferWallet(@PathVariable String walletId, @RequestBody TransferRequest transferRequest) {
        TransactionStatus status = walletService.transferFunds(walletId, transferRequest.fromUserId(), transferRequest.toWalletId(), transferRequest.amount());
        return ResponseEntity.ok(status);
    }

    @GetMapping("/wallets/{walletId}")
    public ResponseEntity<BigDecimal> getWalletBalance(@PathVariable String walletId) {
        return ResponseEntity.ok(walletService.getBalance(walletId));
    }

    @GetMapping("/users/{userId}/wallets")
    public ResponseEntity<List<Wallet>> getUserWallets(@PathVariable String userId) {
        return ResponseEntity.ok(walletService.getUserWallets(userId));
    }
}
