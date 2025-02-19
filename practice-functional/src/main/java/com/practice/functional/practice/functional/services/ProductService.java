package com.practice.functional.practice.functional.services;

import com.practice.functional.practice.functional.dto.ProductDTO;
import com.practice.functional.practice.functional.models.Product;
import com.practice.functional.practice.functional.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    @Autowired
    private final ProductRepository productRepository;

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


    // Cache con ConcurrentHashMap para evitar cosultas repetitivas
    private final Map<Long, ProductDTO> productCache = new ConcurrentHashMap<>();

    // Cola para manejar productos recientes
    private final Queue<Product> recentProducts = new LinkedList<>();

    // Mantiene productos ordenados por precio en tiempo real
    private final NavigableSet<Product> sortProducts = new TreeSet<>(Comparator.comparing(Product::getPrice));

    // Lista enlazada para historico de modificaciones
    private final Deque<Product> modifyProducts = new LinkedList<>();

    // Set para evitar productor duplicados por nombre
    private final Set<String> uniqueProductNames = new HashSet<>();

    public List<ProductDTO> getAllProducts() {
        List<Product> products = productRepository.findAll();

        if (products.isEmpty()) {
            System.out.println("NO hay productos en la base de datos. Cargando productos de prueba...");
            products = defaultProductSupplier.get();
        }

        return products.stream()
                .map(this.productToDto)
                .toList();
    }

    public ProductDTO addProduct(ProductDTO productDTO) {
        Product product = new Product();
        product.setName(productDTO.name());
        product.setPrice(productDTO.price());

        productRepository.save(product);
        logProduct.accept(product);

        // Agregamos el producto a las estructuras de datos
        sortProducts.add(product);
        recentProducts.offer(product);
        modifyProducts.push(product);
        productCache.putIfAbsent(product.getId(), productDTO);
        if (recentProducts.size() > 5) {
            recentProducts.poll();
        }
        return productDTO;
    }

    public List<ProductDTO> getSortedProducts() {
        if (sortProducts.isEmpty()) {
            sortProducts.addAll(productRepository.findAll());
        }

        return sortProducts.stream()
                .map(this.productToDto)
                .toList();
    }

    public List<ProductDTO> getRecentProducts(int limit) {
        List<Product> recentList = new ArrayList<>(recentProducts);

        if (recentList.isEmpty()) {
            recentList = productRepository.findAll()
                    .stream()
                    .sorted(Comparator.comparing(Product::getId).reversed())
                    .limit(limit)
                    .toList();
        }

        return recentList.stream()
                .map(this.productToDto)
                .toList();
    }

    public List<ProductDTO> getModifyProducts(int limit) {
        return modifyProducts.stream()
                .limit(limit)
                .map(this.productToDto)
                .toList();
    }

    // Agrupando productos por precio
    public Map<Double, List<ProductDTO>> groupProductsByPrice() {
        return productRepository.findAll().stream()
                .map(this.productToDto)
                .collect(Collectors.groupingBy(ProductDTO::price));
    }

    // Obteniendo el max y min
    public Map<String, ProductDTO> getMinMaxPrice() {
        return Map.of(
                "Min", Objects.requireNonNull(productRepository.findAll().stream()
                        .min(Comparator.comparing(Product::getPrice))
                        .map(this.productToDto)
                        .orElse(null)),
                "Max", Objects.requireNonNull(productRepository.findAll().stream()
                        .max(Comparator.comparing(Product::getPrice))
                        .map(this.productToDto)
                        .orElse(null))
        );
    }

    // Obtener los productos mas baratos
    public List<ProductDTO> getCheapProducts() {
        return productRepository.findAll()
                .stream()
                .filter(isCheap)
                .map(this.productToDto)
                .toList();
    }

    // tarea hacer un metodo para evitar productos duplicados -> uniqueProductNames
}
