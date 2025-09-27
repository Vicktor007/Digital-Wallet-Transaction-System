package com.vic.walletservice.Mappers;

import com.vic.walletservice.Dtos.CreateWalletRequest;
import com.vic.walletservice.Dtos.CreateWalletResponse;
import com.vic.walletservice.Enums.EventTypes;
import com.vic.walletservice.Models.Wallet;

public class WalletMapper {

    public static CreateWalletResponse toDto(Wallet wallet) {
    return new CreateWalletResponse(
            EventTypes.WALLET_CREATED,
            wallet.getId(),
            wallet.getUser_id(),
            wallet.getBalance(),
            wallet.getCreatedAt()
    );
    }

    public static Wallet toModel(CreateWalletRequest createWalletRequest) {
        Wallet wallet = new Wallet();
        wallet.setUser_id(createWalletRequest.userId());

        return wallet;
    }
}
