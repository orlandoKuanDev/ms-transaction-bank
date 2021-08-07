package com.example.mstransaction.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {
    private final String MICRO_RETIRE_URL = "http://service-retire";
    @Bean
    public WebClient microRetireClient(WebClient.Builder webClientBuilder) {
        return webClientBuilder
                .clone()
                .baseUrl(MICRO_RETIRE_URL)
                .build();
    }
}