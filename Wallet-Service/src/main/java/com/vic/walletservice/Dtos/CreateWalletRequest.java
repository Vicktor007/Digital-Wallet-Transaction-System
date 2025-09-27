package com.vic.walletservice.Dtos;

import jakarta.validation.constraints.NotBlank;

public record CreateWalletRequest(
        @NotBlank  String userId

) {
}
