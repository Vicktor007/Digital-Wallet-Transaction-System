package com.vic.walletservice.Dtos;

import java.math.BigDecimal;

public record FundWalletRequest(
        String userId,
        BigDecimal amount
) {
}
