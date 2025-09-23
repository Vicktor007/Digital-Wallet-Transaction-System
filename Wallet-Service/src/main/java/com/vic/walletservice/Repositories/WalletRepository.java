package com.vic.walletservice.Repositories;

import com.vic.walletservice.Models.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WalletRepository extends JpaRepository<Wallet, String> {
}
