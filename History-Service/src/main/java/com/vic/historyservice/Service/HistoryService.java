package com.vic.historyservice.Service;

import com.vic.historyservice.Models.Transaction_events;
import com.vic.historyservice.Repository.TransactionEventsRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HistoryService {

    private final TransactionEventsRepository eventsRepository;

    public HistoryService(TransactionEventsRepository eventsRepository) {
        this.eventsRepository = eventsRepository;
    }

    public List<Transaction_events> getWalletHistory(String walletId) {
        return eventsRepository.findByWalletId(walletId);
    }

    public List<Transaction_events> getUserHistory(String userId) {
        return eventsRepository.findByUserId(userId);
    }
}
