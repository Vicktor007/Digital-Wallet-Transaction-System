package com.vic.walletservice;



import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.utility.DockerImageName;
import org.junit.jupiter.api.BeforeAll;

@SpringBootTest
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public abstract class KafkaIntegrationTestBase {

    static KafkaContainer kafkaContainer =
            new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.7.0"));

    @BeforeAll
    static void startKafka() {
        if (!kafkaContainer.isRunning()) {
            kafkaContainer.start();
            System.setProperty("spring.kafka.bootstrap-servers", kafkaContainer.getBootstrapServers());
        }
    }
}
