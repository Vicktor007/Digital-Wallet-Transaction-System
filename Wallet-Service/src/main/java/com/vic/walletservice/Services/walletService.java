package com.vic.walletservice.Services;


import com.vic.walletservice.Dtos.WalletEvent;
import com.vic.walletservice.Enums.EventTypes;
import com.vic.walletservice.Enums.TransactionStatus;
import com.vic.walletservice.Enums.TransactionType;
import com.vic.walletservice.Kafka.KafkaProducer;
import com.vic.walletservice.Mappers.WalletMapper;
import com.vic.walletservice.Models.Wallet;
import com.vic.walletservice.Models.Wallet_transactions;
import com.vic.walletservice.Repositories.WalletRepository;
import com.vic.walletservice.Repositories.Wallet_Transactions_Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;



@Service
public class walletService {

    private final WalletRepository walletRepository;
    private final Wallet_Transactions_Repository transactionsRepository;
    private final KafkaProducer kafkaProducer;

    private final Logger log = LoggerFactory.getLogger(walletService.class);


    public walletService(WalletRepository walletRepository, Wallet_Transactions_Repository transactionsRepository, KafkaProducer kafkaProducer) {
        this.walletRepository = walletRepository;
        this.transactionsRepository = transactionsRepository;

        this.kafkaProducer = kafkaProducer;
    }

    public String createWallet(String userId) {

        if (userId == null || userId.isEmpty()) {
            throw new IllegalArgumentException("userId is required");
        }

        Wallet newWallet = WalletMapper.toModel(userId);


        Wallet savedWallet = walletRepository.save(newWallet);

        sendKafkaEvent(
                new WalletEvent(
                        EventTypes.WALLET_CREATED,
                        savedWallet.getId(),
                        userId,
                        savedWallet.getBalance(),
                        "",
                        "",
                        "",
                        savedWallet.getCreatedAt()
                )
        );

        return savedWallet.getId();

    }

    public TransactionStatus fundWallet(String walletId, String userId, BigDecimal amount) {
        Wallet wallet = walletRepository.findById(walletId).orElseThrow(() -> new IllegalArgumentException("Wallet not found"));



        Wallet_transactions transactions = new Wallet_transactions();
        transactions.setWallet(wallet);
        transactions.setAmount(amount);
        transactions.setReceiverId(walletId);
        transactions.setSenderId(walletId);
        transactions.setType(TransactionType.FUND);

        TransactionStatus status = TransactionStatus.FAILED;

        if (Objects.equals(userId, wallet.getUserId())) {
            try {
                wallet.setBalance(wallet.getBalance().add(amount));

                walletRepository.save(wallet);

                status = TransactionStatus.COMPLETED;
            } catch (Exception e) {
                log.error("Error while funding wallet", e);

            }
        }
        transactions.setStatus(status);
        transactionsRepository.save(transactions);

        sendKafkaEvent(
                new WalletEvent(
                        (status == TransactionStatus.COMPLETED) ? EventTypes.WALLET_FUNDED : EventTypes.WALLET_FUNDING_FAILED,
                        walletId,
                        userId,
                        amount,
                        walletId,
                        walletId,
                        transactions.getId(),
                        wallet.getUpdatedAt()
                )
        );
        return status;
    }

    public TransactionStatus transferFunds(String fromWalletId, String fromUserId, String toWalletId, BigDecimal amount) {
        if (fromWalletId.equals(toWalletId)) {
            throw new IllegalArgumentException("Cannot transfer funds to the same wallet");
        }

        // deterministic order lock to avoid deadlocks
        String firstId = fromWalletId.compareTo(toWalletId) < 0 ? fromWalletId : toWalletId;
        String secondId = fromWalletId.compareTo(toWalletId) < 0 ? toWalletId : fromWalletId;

        Wallet first = walletRepository.findByIdForUpdate(firstId)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found"));
        Wallet second = walletRepository.findByIdForUpdate(secondId)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found"));

        Wallet fromWallet = fromWalletId.equals(firstId) ? first : second;
        Wallet toWallet = fromWalletId.equals(firstId) ? second : first;

        Wallet_transactions transactionsFrom = new Wallet_transactions();
        transactionsFrom.setWallet(fromWallet);
        transactionsFrom.setAmount(amount);
        transactionsFrom.setSenderId(fromWalletId);
        transactionsFrom.setReceiverId(toWalletId);
        transactionsFrom.setType(TransactionType.TRANSFER_OUT);

        Wallet_transactions transactionTo = new Wallet_transactions();
        transactionTo.setWallet(toWallet);
        transactionTo.setAmount(amount);
        transactionTo.setSenderId(fromWalletId);
        transactionTo.setReceiverId(toWalletId);
        transactionTo.setType(TransactionType.TRANSFER_IN);


        TransactionStatus status = TransactionStatus.FAILED;

        if (fromWallet.getBalance().compareTo(amount) >= 0) {
            try {
                fromWallet.setBalance(fromWallet.getBalance().subtract(amount));
                toWallet.setBalance(toWallet.getBalance().add(amount));

                walletRepository.save(fromWallet);
                walletRepository.save(toWallet);

                status = TransactionStatus.COMPLETED;

            } catch (Exception e) {
                log.error("Fund transfer error", e);
            }


            transactionsFrom.setStatus(status);
            transactionsRepository.save(transactionsFrom);

            sendKafkaEvent(
                    new WalletEvent(
                            (status == TransactionStatus.COMPLETED) ? EventTypes.TRANSFER_COMPLETED : EventTypes.TRANSFER_FAILED,
                            fromWalletId,
                            fromUserId,
                            amount,
                            fromWalletId,
                            toWalletId,
                            transactionsFrom.getId(),
                            fromWallet.getUpdatedAt()
                    )
            );
            transactionTo.setStatus(status);
            transactionsRepository.save(transactionTo);

            sendKafkaEvent(
                    new WalletEvent(
                            (status == TransactionStatus.COMPLETED) ? EventTypes.TRANSFER_COMPLETED : EventTypes.TRANSFER_FAILED,
                            toWalletId,
                            toWallet.getUserId(),
                            amount,
                            fromWalletId,
                            toWalletId,
                            transactionTo.getId(),
                            toWallet.getUpdatedAt()
                    )
            );

        }
        return status;
    }

    public BigDecimal getBalance(String fromWalletId) {
        Wallet wallet = walletRepository.findById(fromWalletId).orElseThrow(() -> new IllegalArgumentException("Wallet not found"));

        return wallet.getBalance();
    }

    public List<Wallet> getUserWallets(String userId) {
      return walletRepository.findByUserId(userId);
    }

    @Async
    public void sendKafkaEvent(WalletEvent walletEventRequest) {
        try {
            kafkaProducer.sendEvent(walletEventRequest);
            log.info("Sent event asynchronously {}", walletEventRequest);
        } catch (Exception e) {
            log.error("Error while sending event asynchronously {}", walletEventRequest, e);
        }
    }
}
