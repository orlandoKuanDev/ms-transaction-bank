package com.example.mstransaction.handler;

import com.example.mstransaction.models.entities.Bill;
import com.example.mstransaction.models.entities.Transaction;
import com.example.mstransaction.services.BillService;
import com.example.mstransaction.services.ITransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class TransactionHandler {

    private final ITransactionService transactionService;
    private final BillService billService;
    @Autowired
    public TransactionHandler(ITransactionService transactionService, BillService billService) {
        this.transactionService = transactionService;
        this.billService = billService;
    }

    public Mono<ServerResponse> findAll(ServerRequest request){
        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                .body(transactionService.findAll(), Transaction.class);
    }

    public Mono<ServerResponse> findById(ServerRequest request){
        String id = request.pathVariable("id");
        return errorHandler(
                transactionService.findById(id).flatMap(p -> ServerResponse.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(p))
                        .switchIfEmpty(ServerResponse.notFound().build())
        );
    }

    public Mono<ServerResponse> findByAccountNumber(ServerRequest request){
        String accountNumber = request.pathVariable("accountNumber");
        return errorHandler(
                billService.findByAccountNumber(accountNumber).flatMap(p -> ServerResponse.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(p))
                        .switchIfEmpty(ServerResponse.notFound().build())
        );
    }

    public Mono<ServerResponse> save(ServerRequest request){
        Mono<Transaction> product = request.bodyToMono(Transaction.class);
        return product.flatMap(transactionService::create)
                .flatMap(p -> ServerResponse.created(URI.create("/api/client/".concat(p.getId())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(p))
                .onErrorResume(error -> {
                    WebClientResponseException errorResponse = (WebClientResponseException) error;
                    if(errorResponse.getStatusCode() == HttpStatus.BAD_REQUEST) {
                        return ServerResponse.badRequest()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(errorResponse.getResponseBodyAsString());
                    }
                    return Mono.error(errorResponse);
                });
    }

    public Mono<ServerResponse> update(ServerRequest request){
        Mono<Transaction> product = request.bodyToMono(Transaction.class);
        String id = request.pathVariable("id");
        return errorHandler(
                product
                        .flatMap(p -> {
                            p.setId(id);
                            return transactionService.update(p);
                        })
                        .flatMap(p-> ServerResponse.created(URI.create("/api/product/".concat(p.getId())))
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(p))
        );
    }

    public Mono<ServerResponse> delete(ServerRequest request){
        String id = request.pathVariable("id");
        return errorHandler(
                transactionService.delete(id).then(ServerResponse.noContent().build())
        );
    }

    public Mono<ServerResponse> findAllByAccountNumber(ServerRequest request){
        String accountNumber = request.pathVariable("accountNumber");
        Mono<Bill> bill = billService.findByAccountNumber(accountNumber);
        return bill.flatMap(acc -> transactionService.findAll()
                .filter(list -> list.getBill().getAccountNumber().equals(acc.getAccountNumber()))
                .collectList())
                .flatMap(list -> ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(list)
                .onErrorResume(e -> Mono.just("Error " + e.getMessage())
                        .flatMap(s -> ServerResponse.ok()
                                .contentType(MediaType.TEXT_PLAIN)
                                .bodyValue(s)))
                .switchIfEmpty(ServerResponse.notFound().build())
        );
//        return errorHandler(
//            transactionService.findAll().filter(list -> list.getBill().getAccountNumber().equals(accountNumber))
//                    .collectList()
//                    .flatMap(list -> ServerResponse.ok()
//                            .contentType(MediaType.APPLICATION_JSON)
//                            .bodyValue(list)
//                            .switchIfEmpty(ServerResponse.notFound().build())
//                    )
//        );
	}
    /*
     Mono<Bill> bill = billService.findByAccountNumber(accountNumber);
		return  errorHandler( bill.flatMap(acc -> {
            Mono<List<Transaction>> listTransactionsByAccountNumber = transactionService.findAllByBill_AccountNumber(acc.getAccountNumber());
            return listTransactionsByAccountNumber;
        }).flatMap(listTransactions -> ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(listTransactions)));

     */
    private Mono<ServerResponse> errorHandler(Mono<ServerResponse> response){
        return response.onErrorResume(error -> {
            WebClientResponseException errorResponse = (WebClientResponseException) error;
            if(errorResponse.getStatusCode() == HttpStatus.NOT_FOUND) {
                Map<String, Object> body = new HashMap<>();
                body.put("error", "the transaction does not exist: ".concat(errorResponse.getMessage()));
                body.put("timestamp", new Date());
                body.put("status", errorResponse.getStatusCode().value());
                return ServerResponse.status(HttpStatus.NOT_FOUND).bodyValue(body);
            }
            return Mono.error(errorResponse);
        });
    }
}
