package com.vic.historyservice.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.vic.historyservice.Dtos.WalletCreationEvent;
import com.vic.historyservice.Mapper.JsonMapper;
import com.vic.historyservice.Models.Transaction_events;
import com.vic.historyservice.Repository.TransactionEventsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class kafkaConsumer {

    private final Logger log = LoggerFactory.getLogger(kafkaConsumer.class);

    private final TransactionEventsRepository transactionEventsRepository;

    private final JsonMapper jsonMapper;

    public kafkaConsumer(TransactionEventsRepository transactionEventsRepository, JsonMapper jsonMapper) {
        this.transactionEventsRepository = transactionEventsRepository;
        this.jsonMapper = jsonMapper;
    }

    @KafkaListener(topics = "wallet_event_topic")
    public void consumeWalletCreationEventNotification(WalletCreationEvent walletCreationEvent) throws JsonProcessingException {
        log.info("Consuming wallet creation event notification :: {}", walletCreationEvent.toString());

       Transaction_events newTransactionEvents = new  Transaction_events();
       newTransactionEvents.setEvent_type(walletCreationEvent.eventType());
       newTransactionEvents.setWallet_id(walletCreationEvent.walletId());
       newTransactionEvents.setUser_id(walletCreationEvent.userId());
       newTransactionEvents.setEventData(jsonMapper.toJson(walletCreationEvent));

       transactionEventsRepository.save(newTransactionEvents);
    }



}
