package com.example.mstransaction.services;

import com.example.mstransaction.models.entities.Customer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Collections;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON;

@Service
public class CustomerService {
    private final WebClient.Builder webClientBuilder;

    Logger logger = LoggerFactory.getLogger(CustomerService.class);

    @Autowired
    public CustomerService(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    public Mono<Customer> findByIdentityNumber(String identityNumber) {
        return webClientBuilder
                .baseUrl("http://SERVICE-CUSTOMER/customer")
                .build()
                .get()
                .uri("/identity/{identityNumber}", Collections.singletonMap("identityNumber", identityNumber))
                .accept(APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatus::isError, response -> {
                    logTraceResponse(logger, response);
                    return Mono.error(new RuntimeException(
                            String.format("THE CUSTOMER DOES WITH IDENTITY NUMBER %s NOT EXIST IN MICRO SERVICE CUSTOMER: ", identityNumber)
                    ));
                })
                .bodyToMono(Customer.class);
    }

    public Mono<Customer> findAllByCustomerHolder(List<Customer> customers) {
        return webClientBuilder
                .baseUrl("http://SERVICE-CUSTOMER/customer")
                .build()
                .get()
                .uri("/identity/{customers}", Collections.singletonMap("customers", customers))
                .accept(APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatus::isError, response -> {
                    logTraceResponse(logger, response);
                    return Mono.error(new RuntimeException(
                            String.format("THE CUSTOMER DOES WITH IDENTITY NUMBER %s NOT EXIST IN MICRO SERVICE CUSTOMER: ", customers)
                    ));
                })
                .bodyToMono(Customer.class);
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
