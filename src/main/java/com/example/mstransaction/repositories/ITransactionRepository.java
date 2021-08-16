package com.example.mstransaction.repositories;

import com.example.mstransaction.models.entities.Transaction;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

public interface ITransactionRepository extends IRepository<Transaction, String>{
    Mono<List<Transaction>> findAllByBill_AccountNumber(String accountNumber);
    Flux<Transaction> findByTransactionDateBetween(LocalDateTime from, LocalDateTime to);
    Flux<Transaction> findByBill_Acquisition_Product_ProductName(String productName);
}