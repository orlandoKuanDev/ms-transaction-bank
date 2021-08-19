package com.example.mstransaction.config;

import com.example.mstransaction.handler.TransactionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RequestPredicates.DELETE;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class RouterConfig {
    @Bean
    public RouterFunction<ServerResponse> rutas(TransactionHandler handler){
        return route(GET("/transaction"), handler::findAll)
                .andRoute(GET("/transaction/{id}"), handler::findById)
                .andRoute(GET("/transaction/acc/{accountNumber}"), handler::findByAccountNumber)
                .andRoute(GET("/transaction/bill/{accountNumber}"), handler::findAllByAccountNumber)
                .andRoute(GET("/transaction/bill/acquisition/{iban}"), handler::findByAcquisitionIban)
                .andRoute(GET("/transaction/acquisition/{accountNumber}"), handler::findByAcquisitionAccountNumber)
                .andRoute(GET("/transaction/period/{period}/dateInit/{dateInit}/product/{productName}"), handler::generateCommissionPerProductInRange)
                .andRoute(GET("/transaction/between/date/{periodDay}"), handler::transactionBetweenDates)
                .andRoute(GET("/transaction/top/date/{dateTop}"), handler::transactionTop)
                .andRoute(GET("/transaction/average/month/{month}"), handler::transactionAverage)
                .andRoute(POST("/transaction/acquisition/update"), handler::updateAcquisition)
                .andRoute(POST("/transaction/create"), handler::save)
                .andRoute(PUT("/transaction/{id}"), handler::update)
                .andRoute(DELETE("/transaction/{id}"), handler::delete);

    }
}
