package com.example.mstransaction.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@Slf4j(topic = "ACQUISITION_WEBCLIENT_SERVICE")
public class AcquisitionService {
    private final WebClient.Builder webClientBuilder;

    @Autowired
    public AcquisitionService(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }
}
