package com.vic.historyservice.Repository;

import com.vic.historyservice.Models.Transaction_events;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionEventsRepository extends JpaRepository<Transaction_events, String> {
}
