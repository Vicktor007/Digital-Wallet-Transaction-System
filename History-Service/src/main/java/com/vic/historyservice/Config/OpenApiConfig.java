package com.vic.historyservice.Config;

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
    public OpenAPI historyServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Vic Wallet Transaction History API")
                        .version("v1.0")
                        .description("""
                                The **Vic History Service API** provides access to wallet and user transaction histories.
                                It allows querying all events linked to wallets and users for auditing, analytics,
                                and transaction tracking purposes.
                                """)
//                        .contact(new Contact()
//                                .name("Victor – API Support")
//                                .email("support@vicwallet.com")
//                                .url("https://vicwallet.com"))
//                        .license(new License()
//                                .name("Apache 2.0")
//                                .url("https://www.apache.org/licenses/LICENSE-2.0.html"))
//                        .termsOfService("https://vicwallet.com/terms"))
                )
                .servers(List.of(
                        new Server().url("http://localhost:8081").description("Local Development Server")
//                        new Server().url("https://api.vicwallet.com/history").description("Production Server")
                ));
    }
}
