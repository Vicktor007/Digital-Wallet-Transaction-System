package com.vic.walletservice.KafkaTest;

import com.vic.walletservice.Dtos.WalletEvent;
import com.vic.walletservice.Enums.EventTypes;
import com.vic.walletservice.Enums.TransactionType;
import com.vic.walletservice.Kafka.KafkaProducer;
import com.vic.walletservice.Models.WalletEventLog;
import com.vic.walletservice.Repositories.WalletEventLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KafkaProducerTest {

    @Mock
    private KafkaTemplate<String, WalletEvent> kafkaTemplate;

    @Mock
    private WalletEventLogRepository walletEventLogRepository;

    @Captor
    private ArgumentCaptor<WalletEventLog> walletEventLogCaptor;

    private KafkaProducer kafkaProducer;

    private WalletEvent testWalletEvent;
    private LocalDateTime testTimestamp;

    @BeforeEach
    void setUp() {
        kafkaProducer = new KafkaProducer(kafkaTemplate, walletEventLogRepository);
        testTimestamp = LocalDateTime.now();

        testWalletEvent = new WalletEvent(
                EventTypes.WALLET_FUNDED,
                "wallet-123",
                "user-456",
                new BigDecimal("100.50"),
                "sender-789",
                "receiver-012",
                "txn-001",
                TransactionType.FUND,
                testTimestamp
        );
    }

    @Test
    void shouldSendEventSuccessfully() {
        CompletableFuture future =
                CompletableFuture.completedFuture(mock(SendResult.class));

        when(kafkaTemplate.send(anyString(), any(WalletEvent.class))).thenReturn(future);

        kafkaProducer.sendEvent(testWalletEvent);

        verify(kafkaTemplate).send("wallet_event_topic", testWalletEvent);
        verify(walletEventLogRepository, never()).existsByTransactionId(anyString());
        verify(walletEventLogRepository, never()).save(any(WalletEventLog.class));
    }

    @Test
    void shouldSaveToEventLog_WhenKafkaSendFails_AndTransactionDoesNotExist() {
        CompletableFuture<SendResult<String, WalletEvent>> future =
                new CompletableFuture<>();
        future.completeExceptionally(new RuntimeException("Kafka broker unavailable"));

        when(kafkaTemplate.send(anyString(), any(WalletEvent.class))).thenReturn(future);
        when(walletEventLogRepository.existsByTransactionId("txn-001")).thenReturn(false);

        kafkaProducer.sendEvent(testWalletEvent);

        try { Thread.sleep(100); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }

        verify(kafkaTemplate).send("wallet_event_topic", testWalletEvent);
        verify(walletEventLogRepository).existsByTransactionId("txn-001");
        verify(walletEventLogRepository).save(walletEventLogCaptor.capture());

        WalletEventLog savedLog = walletEventLogCaptor.getValue();
        assertThat(savedLog.getEventType()).isEqualTo(EventTypes.WALLET_FUNDED);
        assertThat(savedLog.getWalletId()).isEqualTo("wallet-123");
        assertThat(savedLog.getUserId()).isEqualTo("user-456");
        assertThat(savedLog.getAmount()).isEqualTo(new BigDecimal("100.50"));
        assertThat(savedLog.getSenderId()).isEqualTo("sender-789");
        assertThat(savedLog.getReceiverId()).isEqualTo("receiver-012");
        assertThat(savedLog.getTransactionId()).isEqualTo("txn-001");
        assertThat(savedLog.getTimeStamp()).isEqualTo(testTimestamp);
        assertThat(savedLog.isSent()).isFalse();
    }

    @Test
    void shouldNotSaveToEventLog_WhenKafkaSendFails_ButTransactionAlreadyExists() {
        CompletableFuture<SendResult<String, WalletEvent>> future =
                new CompletableFuture<>();
        future.completeExceptionally(new RuntimeException("Kafka broker unavailable"));

        when(kafkaTemplate.send(anyString(), any(WalletEvent.class))).thenReturn(future);
        when(walletEventLogRepository.existsByTransactionId("txn-001")).thenReturn(true);

        kafkaProducer.sendEvent(testWalletEvent);

        try { Thread.sleep(100); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }

        verify(kafkaTemplate).send("wallet_event_topic", testWalletEvent);
        verify(walletEventLogRepository).existsByTransactionId("txn-001");
        verify(walletEventLogRepository, never()).save(any(WalletEventLog.class));
    }

    @Test
    void shouldNotSaveToEventLog_WhenKafkaSendFails_AndTransactionIdIsNull() {
        WalletEvent eventWithNullTransactionId = new WalletEvent(
                EventTypes.WALLET_FUNDED,
                "wallet-123",
                "user-456",
                new BigDecimal("100.50"),
                "sender-789",
                "receiver-012",
                null, // null transaction ID
                TransactionType.FUND,
                testTimestamp
        );

        CompletableFuture<SendResult<String, WalletEvent>> future =
                new CompletableFuture<>();
        future.completeExceptionally(new RuntimeException("Kafka broker unavailable"));

        when(kafkaTemplate.send(anyString(), any(WalletEvent.class))).thenReturn(future);

        kafkaProducer.sendEvent(eventWithNullTransactionId);

        try { Thread.sleep(100); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }

        verify(kafkaTemplate).send("wallet_event_topic", eventWithNullTransactionId);
        verify(walletEventLogRepository, never()).existsByTransactionId(anyString());
        verify(walletEventLogRepository, never()).save(any(WalletEventLog.class));
    }

    @Test
    void shouldHandleDifferentEventTypes() {
        WalletEvent walletCreatedEvent = new WalletEvent(
                EventTypes.WALLET_CREATED,
                "wallet-new",
                "user-new",
                BigDecimal.ZERO,
                "system",
                "wallet-new",
                "create-txn",
                TransactionType.CREATE_WALLET,
                testTimestamp
        );

        CompletableFuture<SendResult<String, WalletEvent>> future =
                CompletableFuture.completedFuture(mock(SendResult.class));

        when(kafkaTemplate.send(anyString(), any(WalletEvent.class))).thenReturn(future);

        kafkaProducer.sendEvent(walletCreatedEvent);

        verify(kafkaTemplate).send("wallet_event_topic", walletCreatedEvent);
    }

    @Test
    void shouldHandleFailedTransferEvent() {
        WalletEvent failedTransferEvent = new WalletEvent(
                EventTypes.TRANSFER_FAILED,
                "wallet-123",
                "user-456",
                new BigDecimal("50.00"),
                "wallet-123",
                "wallet-999",
                "transfer-fail-txn",
                TransactionType.TRANSFER_OUT,
                testTimestamp
        );

        CompletableFuture<SendResult<String, WalletEvent>> future =
                new CompletableFuture<>();
        future.completeExceptionally(new RuntimeException("Kafka timeout"));

        when(kafkaTemplate.send(anyString(), any(WalletEvent.class))).thenReturn(future);
        when(walletEventLogRepository.existsByTransactionId("transfer-fail-txn")).thenReturn(false);

        kafkaProducer.sendEvent(failedTransferEvent);

        try { Thread.sleep(100); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }

        verify(walletEventLogRepository).save(walletEventLogCaptor.capture());

        WalletEventLog savedLog = walletEventLogCaptor.getValue();
        assertThat(savedLog.getEventType()).isEqualTo(EventTypes.TRANSFER_FAILED);
        assertThat(savedLog.getTransactionId()).isEqualTo("transfer-fail-txn");
        assertThat(savedLog.isSent()).isFalse();
    }
}