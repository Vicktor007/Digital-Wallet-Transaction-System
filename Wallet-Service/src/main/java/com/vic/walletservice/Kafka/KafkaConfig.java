package com.vic.walletservice.Kafka;

import com.vic.walletservice.Dtos.WalletEventRequest;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic walletEventTopic() {
        return TopicBuilder.name("wallet_event_topic")
                .partitions(3)
                .replicas(1)
                .config("retention.ms", String.valueOf(172800000)) // 2 days
                .config("retention.bytes", "-1") // no size limit
                .build();
    }

}
