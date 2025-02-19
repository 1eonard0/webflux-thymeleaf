package com.bolsadeideas.springboot.webflux.app.model.repository;

import com.bolsadeideas.springboot.webflux.app.model.document.Category;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends ReactiveMongoRepository<Category, String> {
}
