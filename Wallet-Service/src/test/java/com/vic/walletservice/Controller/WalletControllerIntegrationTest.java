package com.vic.walletservice.Controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.vic.walletservice.Enums.TransactionStatus;
import com.vic.walletservice.Models.Wallet;
import com.vic.walletservice.Repositories.WalletRepository;
import com.vic.walletservice.Repositories.Wallet_Transactions_Repository;
import com.vic.walletservice.Services.walletService;
import com.vic.walletservice.Kafka.KafkaProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class WalletControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private Wallet_Transactions_Repository txRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private walletService walletService;

    // ✅ KafkaProducer is mocked to isolate service logic
    @MockitoBean
    private KafkaProducer kafkaProducer;

    private String walletA;
    private String walletB;

    @BeforeEach
    void setUp() {
        // Reset DB state
        txRepository.deleteAll();
        walletRepository.deleteAll();

        walletA = walletService.createWallet("userA");
        walletB = walletService.createWallet("userB");

        walletService.fundWallet(walletA, "userA", new BigDecimal("200.00"));
    }

    @Test
    void transferFundsBetweenWallets_ShouldTransferSuccessfully() throws Exception {
        var request = new TransferRequest("userA", walletB, new BigDecimal("50.00"));

        mockMvc.perform(post("/wallets/{walletId}/transfer", walletA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string(TransactionStatus.COMPLETED.name()));

        Wallet from = walletRepository.findById(walletA).orElseThrow();
        Wallet to = walletRepository.findById(walletB).orElseThrow();

        assertThat(from.getBalance()).isEqualByComparingTo("150.00");
        assertThat(to.getBalance()).isEqualByComparingTo("50.00");
    }

    @Test
    void transferFundsBetweenWallets_ShouldFail_WhenInsufficientFunds() throws Exception {
        var request = new TransferRequest("userA", walletB, new BigDecimal("5000.00"));

        mockMvc.perform(post("/wallets/{walletId}/transfer", walletA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string(TransactionStatus.FAILED.name()));

        Wallet from = walletRepository.findById(walletA).orElseThrow();
        Wallet to = walletRepository.findById(walletB).orElseThrow();

        assertThat(from.getBalance()).isEqualByComparingTo("200.00");
        assertThat(to.getBalance()).isEqualByComparingTo("0.00");
    }

    @Test
    void getUserWallets_ShouldReturnAllWalletsForUser() throws Exception {
        // userA has 1 wallet (walletA)
        mockMvc.perform(get("/wallets/users/{userId}/wallets", "userA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value("userA"))
                .andExpect(jsonPath("$[0].balance").value(200.00));
    }

    record TransferRequest(String fromUserId, String toWalletId, BigDecimal amount) {}
}

