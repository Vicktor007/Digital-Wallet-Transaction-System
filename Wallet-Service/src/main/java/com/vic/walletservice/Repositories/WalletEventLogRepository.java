package com.vic.walletservice.Repositories;

import com.vic.walletservice.Models.WalletEventLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WalletEventLogRepository extends JpaRepository<WalletEventLog, Long> {
    List<WalletEventLog> findBySentFalse();

    boolean existsByTransactionId(String transactionId);
}
