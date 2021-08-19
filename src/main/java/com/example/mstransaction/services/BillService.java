package com.example.mstransaction.services;

import com.example.mstransaction.exception.webclient.ArgumentWebClientNotValid;
import com.example.mstransaction.models.entities.Bill;
import com.example.mstransaction.utils.CustomMessage;
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

import java.util.Collections;

import static org.springframework.http.MediaType.APPLICATION_JSON;

@Service
public class BillService {
    private final WebClient.Builder webClientBuilder;
    private final CustomMessage customMessage;
    Logger logger = LoggerFactory.getLogger(BillService.class);
    @Autowired
    public BillService(WebClient.Builder webClientBuilder, CustomMessage customMessage) {
        this.webClientBuilder = webClientBuilder;
        this.customMessage = customMessage;
    }

    public Mono<Bill> findByAccountNumber(String accountNumber) {
        return webClientBuilder
                .baseUrl("http://SERVICE-BILL/bill")
                .build()
                .get()
                .uri("/acc/{accountNumber}", Collections.singletonMap("accountNumber", accountNumber))
                .accept(APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatus::isError, response -> {
                    logTraceResponse(logger, response);
                    return Mono.error(new ArgumentWebClientNotValid(
                            String.format("THE ACCOUNT NUMBER DONT EXIST IN MICRO SERVICE BILL-> %s", accountNumber)
                    ));
                })
                .bodyToMono(Bill.class);
    }

    public Mono<Bill> findByIban(String iban) {
        return webClientBuilder
                .baseUrl("http://SERVICE-BILL/bill")
                .build()
                .get()
                .uri("/acquisition/{iban}", Collections.singletonMap("iban", iban))
                .accept(APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatus::isError, response -> {
                    logTraceResponse(logger, response);
                    return Mono.error(new ArgumentWebClientNotValid(
                            String.format("THE IBAN DONT EXIST IN MICRO SERVICE BILL-> %s", iban)
                    ));
                })
                .bodyToMono(Bill.class);
    }

    public Mono<Bill> updateBill(Bill bill){
        logger.info("BILL_WEBCLIENT_UPDATE {}", bill);
        return webClientBuilder
                .baseUrl("http://SERVICE-BILL/bill")
                .build()
                .post()
                .uri("/update")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(Mono.just(bill), Bill.class)
                .retrieve()
                .onStatus(HttpStatus::isError, response -> {
                    logTraceResponse(logger, response);
                    return Mono.error(new RuntimeException("THE BILL UPDATE FAILED"));
                })
                .bodyToMono(Bill.class);
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
