package com.practice.functional.practice.functional.services;

import com.practice.functional.practice.functional.dto.ProductDTO;
import com.practice.functional.practice.functional.models.Product;
import com.practice.functional.practice.functional.repositories.ProductRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.*;
import java.util.function.*;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    private final Supplier<List<Product>> defaultProductSupplier = () -> List.of(
            new Product(1L, "Laptop gamer", 120.00),
            new Product(2L, "Keyboard", 20.00),
            new Product(3L, "Mouse", 12.00)
    );

    private final Consumer<Product> logProduct =  product ->
        System.out.println("Producto agregado fue: " + product.getName() + " con precio de: " + product.getPrice());


    private final Predicate<Product> isCheap = product ->
            product.getPrice()<20.00;

    private final Function<Product, ProductDTO> productToDto = product ->
            new ProductDTO(product.getName(), product.getPrice());


    // Obtener los productos - map, defaultIfEmpty, switchIf
    public Flux<ProductDTO> getAllProducts() {
        return productRepository.findAll()
                .map(productToDto)
                .switchIfEmpty(Flux.fromIterable(defaultProductSupplier.get()).map(productToDto)
                .delayElements(Duration.ofMillis(500)));
    }

    // Obtener un producto por id - flatmap, defaultIfEmpty, doOnError
    public Mono<ProductDTO> getProductById(String id) {
        return productRepository.findById(id)
                .flatMap(product -> Mono.just(productToDto.apply(product)))
                .doOnError(error -> System.out.println("Error al buscar el producto: " + error.getMessage()))
                .defaultIfEmpty(new ProductDTO("Producto no encontrado", 0.00));
    }

    // Agregar producto - doOnNext
    public Mono<ProductDTO> addProduct(ProductDTO productDTO) {
        return Mono.fromSupplier(() -> {
            Product product = new Product();
            product.setName(productDTO.name());
            product.setPrice(productDTO.price());
            productRepository.save(product);
            return product;
        }).doOnNext(logProduct).map(productToDto);
    }

    // Obtener los productos baratos - filter, map
    public Flux<ProductDTO> getCheapProducts() {
        return productRepository.findAll()
                .filter(isCheap)
                .map(productToDto);
    }

    // Obtener los ultimos 3 agregados - take
    public Flux<ProductDTO> getLastProducts() {
        return productRepository.findAll()
                .take(3)
                .map(productToDto);
    }

    // Eliminar por id - doOnError
    public Mono<Void> deleteProduct(String id) {
        return productRepository.findById(id)
                .flatMap(product -> productRepository.deleteById(id))
                .doOnError(error -> System.out.println("Error al eliminar el producto " + error.getMessage()));
    }

    // Contar productos - collectList, map, doOnNext
    public Mono<Integer> countProducts() {
        return productRepository.findAll()
                .collectList()
                .map(List::size)
                .doOnNext(count -> System.out.println("Cantidad de productos: " + count));
    }

}
