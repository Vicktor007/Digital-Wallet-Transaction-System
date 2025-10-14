package com.vic.walletservice.Kafka;

import com.vic.walletservice.Dtos.WalletEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducer {

    private final Logger logger = LoggerFactory.getLogger(KafkaProducer.class);
    private final KafkaTemplate<String, WalletEvent> kafkaTemplate;

    public KafkaProducer(KafkaTemplate<String, WalletEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendEvent(WalletEvent walletEventRequest) {
        logger.info("Sending event with body {}", walletEventRequest);


        kafkaTemplate.send("wallet_event_topic", walletEventRequest);
    }


}
