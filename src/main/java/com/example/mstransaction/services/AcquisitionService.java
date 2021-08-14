package com.example.mstransaction.services;

import com.example.mstransaction.models.entities.Acquisition;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@Slf4j(topic = "ACQUISITION_WEBCLIENT_SERVICE")
public class AcquisitionService {
    private final WebClient.Builder webClientBuilder;

    Logger logger = LoggerFactory.getLogger(AcquisitionService.class);
    @Autowired
    public AcquisitionService(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    public Mono<Acquisition> updateAcquisition(Acquisition acquisition, String cardNumber){
        logger.info("TRANSACTION_WEBCLIENT_UPDATE {}", acquisition);
        return webClientBuilder
                .baseUrl("http://SERVICE-ACQUISITION/acquisition")
                .build()
                .put()
                .uri("/update/{cardNumber}", cardNumber)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(Mono.just(acquisition), Acquisition.class)
                .retrieve()
                .onStatus(HttpStatus::isError, response -> {
                    logTraceResponse(logger, response);
                    return Mono.error(new RuntimeException("THE ACQUISITION UPDATE FAILED"));
                })
                .bodyToMono(Acquisition.class);
    }

    public static void logTraceResponse(Logger log, ClientResponse response) {
        if (log.isTraceEnabled()) {
            log.trace("Response status: {}", response.statusCode());
            log.trace("Response headers: {}", response.headers().asHttpHeaders());
            response.bodyToMono(String.class)
                    .publishOn(Schedulers.boundedElastic())
                    .subscribe(body -> log.trace("Response body: {}", body));
        }
    }
}


