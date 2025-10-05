package com.vic.walletservice.Kafka;

import com.vic.walletservice.Dtos.WalletEventRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import static org.springframework.kafka.support.KafkaHeaders.TOPIC;

@Service
public class KafkaProducer {

    private final Logger logger = LoggerFactory.getLogger(KafkaProducer.class);
    private final KafkaTemplate<String, WalletEventRequest> kafkaTemplate;

    public KafkaProducer(KafkaTemplate<String, WalletEventRequest> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendEvent(WalletEventRequest walletEventRequest) {
        logger.info("Sending event with body {}", walletEventRequest);


        kafkaTemplate.send("wallet_event_topic", walletEventRequest);
    }

//    public void sendTransactionEvent(WalletTransactionEventRequest transactionEventRequest) {
//        logger.info("Sending event with body {}", transactionEventRequest);
//        Message <WalletTransactionEventRequest> message = MessageBuilder.withPayload(transactionEventRequest)
//                .setHeader(TOPIC, "wallet_transaction_event_topic")
//                .build();
//
//        kafkaTemplate.send(message);
//    }
}
