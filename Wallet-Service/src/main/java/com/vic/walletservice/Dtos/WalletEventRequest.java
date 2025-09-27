package com.vic.walletservice.Dtos;

import com.vic.walletservice.Enums.EventTypes;
import java.time.LocalDateTime;


public record WalletEventRequest(
        EventTypes eventType,
        String walletId,
        String userId,
        String balance
) {}
