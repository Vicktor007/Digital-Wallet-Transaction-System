package com.vic.walletservice.Mappers;

import com.vic.walletservice.Dtos.CreateWalletRequest;
import com.vic.walletservice.Dtos.WalletResponse;
import com.vic.walletservice.Enums.EventTypes;
import com.vic.walletservice.Models.Wallet;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class WalletMapper {


    public static Wallet toModel(String userId) {
        Wallet wallet = new Wallet();
        wallet.setUserId(userId);
        wallet.setBalance(BigDecimal.ZERO);
        wallet.setCreatedAt(LocalDateTime.now());
        wallet.setUpdatedAt(LocalDateTime.now());
        return wallet;
    }
}
