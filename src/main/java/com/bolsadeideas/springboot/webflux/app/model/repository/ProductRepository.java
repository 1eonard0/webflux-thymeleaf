package com.bolsadeideas.springboot.webflux.app.model.repository;

import com.bolsadeideas.springboot.webflux.app.model.document.Product;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends ReactiveMongoRepository<Product, String> {
}
