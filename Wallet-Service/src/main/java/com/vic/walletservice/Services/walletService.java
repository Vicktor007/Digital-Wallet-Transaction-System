package com.vic.walletservice.Services;

import com.vic.walletservice.Dtos.CreateWalletRequest;
import com.vic.walletservice.Dtos.CreateWalletResponse;
import com.vic.walletservice.Dtos.WalletEventRequest;
import com.vic.walletservice.Enums.EventTypes;
import com.vic.walletservice.Kafka.KafkaProducer;
import com.vic.walletservice.Mappers.WalletMapper;
import com.vic.walletservice.Models.Wallet;
import com.vic.walletservice.Repositories.WalletRepository;
import org.springframework.stereotype.Service;

@Service
public class walletService {

    private final WalletRepository walletRepository;
    private final KafkaProducer kafkaProducer;

    public walletService(WalletRepository walletRepository, KafkaProducer kafkaProducer) {
        this.walletRepository = walletRepository;

        this.kafkaProducer = kafkaProducer;
    }

    public CreateWalletResponse createWallet(CreateWalletRequest createWalletRequest) {
        Wallet newWallet = walletRepository.save(WalletMapper.toModel(createWalletRequest));

        kafkaProducer.sendEvent(
                new WalletEventRequest(
                        EventTypes.WALLET_CREATED,
                        createWalletRequest.userId(),
                        newWallet.getId(),
                        newWallet.getBalance().toString()
                )
        );

        return WalletMapper.toDto(newWallet);

    }
}
