package com.vic.walletservice.Kafka;

import com.vic.walletservice.Dtos.WalletEvent;
import com.vic.walletservice.Models.WalletEventLog;
import com.vic.walletservice.Repositories.WalletEventLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducer {

    private static final Logger logger = LoggerFactory.getLogger(KafkaProducer.class);
    private static final String TOPIC = "wallet_event_topic";

    private final KafkaTemplate<String, WalletEvent> kafkaTemplate;
    private final WalletEventLogRepository walletEventLogRepository;

    public KafkaProducer(KafkaTemplate<String, WalletEvent> kafkaTemplate,
                         WalletEventLogRepository walletEventLogRepository) {
        this.kafkaTemplate = kafkaTemplate;
        this.walletEventLogRepository = walletEventLogRepository;
    }

    public void sendEvent(WalletEvent walletEventRequest) {
        logger.info("Sending event with body {}", walletEventRequest);

        var future = kafkaTemplate.send(TOPIC, walletEventRequest);

        // CompletableFuture callback
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                // on Success
                logger.info("Successfully sent event with body {}", walletEventRequest);
            } else {
                // on Failure
                logger.error("Failed to send event with body {}", walletEventRequest, ex);

                String txId = walletEventRequest.transactionId();

                if (txId == null || !walletEventLogRepository.existsByTransactionId(txId)) {
                    WalletEventLog logEntry = getWalletEventLog(walletEventRequest);

                    walletEventLogRepository.save(logEntry);
                }
            }
        });
    }

    private static WalletEventLog getWalletEventLog(WalletEvent walletEventRequest) {
        WalletEventLog logEntry = new WalletEventLog();
        logEntry.setEventType(walletEventRequest.eventType());
        logEntry.setWalletId(walletEventRequest.walletId());
        logEntry.setUserId(walletEventRequest.userId());
        logEntry.setAmount(walletEventRequest.amount());
        logEntry.setSenderId(walletEventRequest.senderId());
        logEntry.setReceiverId(walletEventRequest.receiverId());
        logEntry.setTransactionId(walletEventRequest.transactionId());
        logEntry.setTimeStamp(walletEventRequest.timeStamp());
        logEntry.setSent(false);
        return logEntry;
    }
}
