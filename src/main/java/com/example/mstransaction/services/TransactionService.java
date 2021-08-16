package com.example.mstransaction.services;

import com.example.mstransaction.models.entities.Transaction;
import com.example.mstransaction.repositories.IRepository;
import com.example.mstransaction.repositories.ITransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TransactionService extends BaseService<Transaction, String> implements ITransactionService{

    private final ITransactionRepository repository;

    @Autowired
    public TransactionService(ITransactionRepository repository) {
        this.repository = repository;
    }

    @Override
    protected IRepository<Transaction, String> getRepository() {
        return repository;
    }

    @Override
    public Mono<List<Transaction>> findAllByBill_AccountNumber(String accountNumber) {
        return repository.findAllByBill_AccountNumber(accountNumber);
    }

    @Override
    public Flux<Transaction> findByTransactionDateBetween(LocalDateTime from, LocalDateTime to) {
        return repository.findByTransactionDateBetween(from, to);
    }

    @Override
    public Flux<Transaction> findByBill_Acquisition_Product_ProductName(String productName) {
        return repository.findByBill_Acquisition_Product_ProductName(productName);
    }

}
