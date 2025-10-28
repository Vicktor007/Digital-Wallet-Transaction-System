package com.vic.gatewayservice.Controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
public class GatewayCorsConfig {

    @Value("${frontEndUrl}")
    private String frontEndUrl;

    @Bean
    public CorsWebFilter swaggerCorsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(frontEndUrl));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/v3/api-docs/**", config);
        source.registerCorsConfiguration("/swagger-ui/**", config);

        return new CorsWebFilter(source);
    }
}
