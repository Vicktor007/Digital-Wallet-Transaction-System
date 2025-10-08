package com.vic.walletservice.Dtos;

import com.vic.walletservice.Enums.EventTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;


public record WalletEventRequest(
        EventTypes eventType,
        String walletId,
        String userId,
        BigDecimal amount,
        String senderId,
        String receiverId,
        String transactionId,
        LocalDateTime timeStamp
) {}
