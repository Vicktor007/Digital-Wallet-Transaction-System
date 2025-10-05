package com.vic.historyservice.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.vic.historyservice.Dtos.WalletEvent;
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

    @KafkaListener(topics = "wallet_event_topic", groupId = "wallet_events")
    public void consumeWalletCreationEventNotification(WalletEvent walletEvent) throws JsonProcessingException {
        log.info("Consuming wallet event notification :: {}", walletEvent.toString());

        if (transactionEventsRepository.existsByTransaction_id(walletEvent.transactionId())){
            return;
        }

       Transaction_events newTransactionEvents = new  Transaction_events();
       newTransactionEvents.setEvent_type(walletEvent.eventType());
       newTransactionEvents.setWallet_id(walletEvent.walletId());
       newTransactionEvents.setUser_id(walletEvent.userId());
       newTransactionEvents.setAmount(walletEvent.amount());
       newTransactionEvents.setTransaction_id(walletEvent.transactionId());
       newTransactionEvents.setEventData(jsonMapper.toJson(walletEvent));

       transactionEventsRepository.save(newTransactionEvents);
    }



}
