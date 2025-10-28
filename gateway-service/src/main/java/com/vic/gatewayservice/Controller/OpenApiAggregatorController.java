package com.vic.gatewayservice.Controller;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@RestController
public class OpenApiAggregatorController {

    @Value("${walletServiceUrl}")
    private String walletServiceUrl;

    @Value("${historyServiceUrl}")
    private String historyServiceUrl;

    private final WebClient webClient = WebClient.create();

    @GetMapping("/v3/api-docs")
    public Mono<String> defaultDocs() {
        return walletDocs(); // Reuse the walletDocs() method
    }

    @GetMapping("/v3/api-docs/wallet")
    public Mono<String> walletDocs() {
        return webClient.get()
                .uri(walletServiceUrl + "/v3/api-docs")
                .retrieve()
                .bodyToMono(String.class);
    }

    @GetMapping("/v3/api-docs/history")
    public Mono<String> historyDocs() {
        return webClient.get()
                .uri(historyServiceUrl + "/v3/api-docs")
                .retrieve()
                .bodyToMono(String.class);
    }
}
