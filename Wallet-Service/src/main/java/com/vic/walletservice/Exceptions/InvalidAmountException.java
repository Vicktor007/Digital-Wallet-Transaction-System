package com.vic.walletservice.Exceptions;


import java.math.BigDecimal;

public class InvalidAmountException extends RuntimeException {
    public InvalidAmountException(String message) {
        super(message);
    }

    public static InvalidAmountException forWallet(BigDecimal currentBalance, BigDecimal requiredAmount) {
        return new InvalidAmountException(
                String.format("Insufficient funds. Current: %s, Required: %s",
                        currentBalance, requiredAmount)
        );
    }
}