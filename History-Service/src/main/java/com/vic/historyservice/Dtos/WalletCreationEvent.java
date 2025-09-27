package com.vic.historyservice.Dtos;

import com.vic.historyservice.Enums.EventTypes;

public record WalletCreationEvent(

        EventTypes eventType,
        String userId,
        String walletId,
        String balance,
        String createdAt
) {
}
