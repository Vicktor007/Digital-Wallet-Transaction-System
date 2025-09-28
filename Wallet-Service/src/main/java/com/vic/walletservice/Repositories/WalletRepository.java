package com.vic.walletservice.Repositories;

import com.vic.walletservice.Models.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WalletRepository extends JpaRepository<Wallet, String> {
    List<Wallet> findByUserId(String userId);
}
