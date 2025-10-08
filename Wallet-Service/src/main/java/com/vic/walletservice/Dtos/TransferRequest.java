package com.vic.walletservice.Dtos;

import java.math.BigDecimal;

public record TransferRequest(
        String fromUserId, String toWalletId, BigDecimal amount
) {
}
