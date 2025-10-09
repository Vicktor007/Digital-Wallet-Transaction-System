package com.vic.walletservice.Config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI walletServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Digital Wallet Transaction System API")
                        .version("v1.0")
                        .description("""
                                **Digital Wallet Transaction System** — a simple and secure digital wallet
                                platform for managing funds, transferring money, sending notifications and tracking user activities.
                                """)
//                        .contact(new Contact()
//                                .name("Victor – API Support")
//                                .email("support@vicwallet.com")
//                                .url("https://vicwallet.com"))
//
                )
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local Development Server")
//                        new Server().url("https://api.vicwallet.com").description("Production Server")
                ));
    }
}
