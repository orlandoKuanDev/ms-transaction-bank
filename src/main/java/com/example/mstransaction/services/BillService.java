package com.example.mstransaction.services;

import com.example.mstransaction.models.entities.Bill;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.TcpClient;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8;

@Service
public class BillService {
    private final WebClient.Builder webClientBuilder;
    @Autowired
    public BillService(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    //define timeout
    TcpClient tcpClient = TcpClient
            .create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
            .doOnConnected(connection -> {
                connection.addHandlerLast(new ReadTimeoutHandler(5000, TimeUnit.MILLISECONDS));
                connection.addHandlerLast(new WriteTimeoutHandler(5000, TimeUnit.MILLISECONDS));
            });

    /*public Mono<Bill> findByAccountNumber(String accountNumber) {
        WebClient client = webClientBuilder.clientConnector(new ReactorClientHttpConnector(HttpClient.from(tcpClient)))
                .baseUrl("http://SERVICE-BILL")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultUriVariables(Collections.singletonMap("url", "http://SERVICE-BILL"))
                .build();

        return client.get()
                .uri("/{accountNumber}", Collections.singletonMap("accountNumber", accountNumber))
                .accept(APPLICATION_JSON)
                .retrieve()
                .bodyToMono(Bill.class);
    }*/
    public Mono<Bill> findByAccountNumber(String accountNumber) {
        return webClientBuilder.build().get().uri("/acc/{accountNumber}", Collections.singletonMap("accountNumber", accountNumber))
                .accept(APPLICATION_JSON)
                .retrieve()
                .bodyToMono(Bill.class);
    }

}
