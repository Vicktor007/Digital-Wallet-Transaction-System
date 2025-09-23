package com.vic.walletservice.Models;

import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedDate;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
public class Wallet {
    @Id
    @GeneratedValue(generator = "uuid")
    @UuidGenerator
    @Column(length = 36, nullable = false, updatable = false)
    private String id;

    @GeneratedValue(generator = "uuid")
    @UuidGenerator
    @Column(length = 100, nullable = false, unique = true)
    private String user_id;

    @Column(precision = 19, scale = 4, nullable = false)
    private BigDecimal balance = BigDecimal.ZERO;

    @Version
    @Column(nullable = false)
    private Long version = 0L;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @CreatedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
