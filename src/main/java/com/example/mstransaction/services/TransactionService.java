package com.example.mstransaction.services;

import com.example.mstransaction.models.entities.Transaction;
import com.example.mstransaction.repositories.IRepository;
import com.example.mstransaction.repositories.ITransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
}
