package com.vic.walletservice.Services;

import com.vic.walletservice.Dtos.CreateWalletRequest;
import com.vic.walletservice.Dtos.WalletResponse;
import com.vic.walletservice.Dtos.WalletEventRequest;
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
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@Service
public class walletService {

    private final WalletRepository walletRepository;
    private final Wallet_Transactions_Repository transactionsRepository;
    private final KafkaProducer kafkaProducer;

    private final Logger log = LoggerFactory.getLogger(walletService.class);
    private final Wallet_Transactions_Repository wallet_Transactions_Repository;

    public walletService(WalletRepository walletRepository, Wallet_Transactions_Repository transactionsRepository, KafkaProducer kafkaProducer, Wallet_Transactions_Repository wallet_Transactions_Repository) {
        this.walletRepository = walletRepository;
        this.transactionsRepository = transactionsRepository;

        this.kafkaProducer = kafkaProducer;
        this.wallet_Transactions_Repository = wallet_Transactions_Repository;
    }

    public WalletResponse createWallet(CreateWalletRequest createWalletRequest) {
        Wallet newWallet = walletRepository.save(WalletMapper.toModel(createWalletRequest));

        kafkaProducer.sendEvent(
                new WalletEventRequest(
                        EventTypes.WALLET_CREATED,
                        newWallet.getId(),
                        createWalletRequest.userId(),
                        newWallet.getBalance(),
                        "",
                        newWallet.getCreatedAt()
                )
        );

        return WalletMapper.toDto(newWallet);

    }

    public WalletResponse fundWallet(String walletId, String userId, BigDecimal amount) {
        Wallet wallet = walletRepository.findById(walletId).orElseThrow(() -> new IllegalArgumentException("Wallet not found"));



        Wallet_transactions transactions = new Wallet_transactions();
        transactions.setWallet(wallet);
        transactions.setAmount(amount);
        transactions.setType(TransactionType.FUND);
        TransactionStatus status;


        try {
            wallet.setBalance(wallet.getBalance().add(amount));

            walletRepository.save(wallet);

            status = TransactionStatus.COMPLETED;
        } catch (Exception e) {
            status = TransactionStatus.FAILED;
            log.error("Error while funding wallet", e);

        }

        transactions.setStatus(status);
        transactionsRepository.save(transactions);

        kafkaProducer.sendEvent(
                new WalletEventRequest(
                        (status == TransactionStatus.COMPLETED) ? EventTypes.WALLET_FUNDED : EventTypes.WALLET_FUNDING_FAILED,
                        walletId,
                        userId,
                        amount,
                        transactions.getId(),
                        wallet.getUpdatedAt()
                )
        );
        return WalletMapper.toDto(wallet);
    }

    public TransactionStatus transferFunds(String fromWalletId, String fromUserId, String toWalletId, BigDecimal amount) {
        if (fromWalletId.equals(toWalletId)) {
            throw new IllegalArgumentException("Cannot transfer funds to the same wallet");
        }

        Wallet fromWallet = walletRepository.findById(fromWalletId).orElseThrow(() -> new IllegalArgumentException("Wallet not found"));
        Wallet toWallet = walletRepository.findById(toWalletId).orElseThrow(() -> new IllegalArgumentException("Wallet not found"));

        Wallet_transactions transactionsFrom = new Wallet_transactions();
        transactionsFrom.setWallet(fromWallet);
        transactionsFrom.setAmount(amount);
        transactionsFrom.setType(TransactionType.TRANSFER_OUT);

        Wallet_transactions transactionTo = new Wallet_transactions();
        transactionTo.setWallet(toWallet);
        transactionTo.setAmount(amount);
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
                status = TransactionStatus.FAILED;
                log.error("Fund transfer error", e);
            }


            transactionsFrom.setStatus(status);
            transactionsRepository.save(transactionsFrom);

            kafkaProducer.sendEvent(
                    new WalletEventRequest(
                            (status == TransactionStatus.COMPLETED) ? EventTypes.WALLET_FUNDED : EventTypes.WALLET_FUNDING_FAILED,
                            fromWalletId,
                            fromUserId,
                            amount,
                            transactionsFrom.getId(),
                            fromWallet.getUpdatedAt()
                    )
            );
            transactionTo.setStatus(status);
            transactionsRepository.save(transactionTo);

            kafkaProducer.sendEvent(
                    new WalletEventRequest(
                            (status == TransactionStatus.COMPLETED) ? EventTypes.TRANSFER_COMPLETED : EventTypes.TRANSFER_FAILED,
                            toWalletId,
                            toWallet.getUser_id(),
                            amount,
                            transactionTo.getId(),
                            toWallet.getUpdatedAt()
                    )
            );

        }
        return status;
    }

    public BigDecimal getBalance(String fromWalletId, String fromUserId) {
        Wallet wallet = walletRepository.findById(fromWalletId).orElseThrow(() -> new IllegalArgumentException("Wallet not found"));

        return wallet.getBalance();
    }

    public List<Wallet> getUserWallets(String userId) {
      return walletRepository.findByUserId(userId);
    }
}
