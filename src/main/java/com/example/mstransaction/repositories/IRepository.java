package com.example.mstransaction.repositories;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface IRepository<T,ID> extends ReactiveMongoRepository<T, ID> {
}
