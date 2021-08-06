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
                .andRoute(POST("/transaction"), handler::save)
                .andRoute(PUT("/transaction/{id}"), handler::update)
                .andRoute(DELETE("/transaction/{id}"), handler::delete);

    }
}
