package com.practice.functional.practice.functional.controllers;

import com.practice.functional.practice.functional.dto.ProductDTO;
import com.practice.functional.practice.functional.services.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@Controller
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public Flux<ProductDTO> getAllProducts() {
        return productService.getAllProducts();
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<ProductDTO>> getProductById(@PathVariable String id) {
        return productService.getProductById(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Mono<ResponseEntity<ProductDTO>> addProduct(@Valid @RequestBody ProductDTO productDTO) {
        return productService.addProduct(productDTO)
                .map(product -> ResponseEntity.status(HttpStatus.CREATED).body(product));
    }

    @GetMapping("/cheap")
    public Flux<ProductDTO> getCheapProducts() {
        return productService.getCheapProducts();
    }

    @GetMapping("/last")
    public Flux<ProductDTO> getLastProducts() {
        return productService.getLastProducts();
    }

    @GetMapping("/count")
    public Mono<Integer> countProducts() {
        return productService.countProducts();
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteProduct(@PathVariable String id) {
        return productService.deleteProduct(id)
                .then(Mono.just(ResponseEntity.noContent().build()));
    }
}
