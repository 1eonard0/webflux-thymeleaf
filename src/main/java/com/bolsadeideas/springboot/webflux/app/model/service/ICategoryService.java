package com.bolsadeideas.springboot.webflux.app.model.service;

import com.bolsadeideas.springboot.webflux.app.model.document.Category;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ICategoryService {

    public Flux<Category> findAll();

    public Mono<Category> findById(String id);

    public Mono<Category> save(Category category);

}
