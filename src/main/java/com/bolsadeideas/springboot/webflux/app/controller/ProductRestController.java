package com.bolsadeideas.springboot.webflux.app.controller;

import com.bolsadeideas.springboot.webflux.app.model.document.Product;
import com.bolsadeideas.springboot.webflux.app.model.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/v1/products")
public class ProductRestController {

    @Autowired
    private ProductRepository productRepository;

    @GetMapping("/list-all")
    public Flux<Product> listAll(Model model){
        return productRepository.findAll()
                .map(product -> {
                    product.setName(product.getName().toUpperCase());
                    return product;
                })
                .log();
    }
}
