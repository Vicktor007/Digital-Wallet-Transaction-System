package com.vic.historyservice.Models;


import com.vic.historyservice.Enums.EventTypes;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Unique ID for this transaction event", example = "tx-event-001")
@Entity
@Table(name = "Transaction_events")
@EntityListeners(AuditingEntityListener.class)
public class Transaction_events {
    @Id
    @GeneratedValue(generator = "uuid")
    @UuidGenerator
    @Column(length = 36, nullable = false, updatable = false)
    private String id;

    @Column(length = 36, nullable = false)
    private String wallet_id;

    @Column(length = 100, nullable = false)
    private String user_id;

    @Column(precision = 19, scale = 4, nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(length = 30, nullable = false)
    private EventTypes event_type;

    @Column(length = 36)
    private String transaction_id;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(columnDefinition = "jsonb", name = "event_data")
    private String eventData;

    public Transaction_events() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getWallet_id() {
        return wallet_id;
    }

    public void setWallet_id(String wallet_id) {
        this.wallet_id = wallet_id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public EventTypes getEvent_type() {
        return event_type;
    }

    public void setEvent_type(EventTypes event_type) {
        this.event_type = event_type;
    }

    public String getTransaction_id() {
        return transaction_id;
    }

    public void setTransaction_id(String transaction_id) {
        this.transaction_id = transaction_id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getEventData() {
        return eventData;
    }

    public void setEventData(String eventData) {
        this.eventData = eventData;
    }
}
