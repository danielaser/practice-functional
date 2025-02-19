package com.practice.functional.practice.functional.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "products")
public class Product implements Comparable<Product> {

    @Id
    private Long id;

    private String name;
    private Double price;

    public Product(Double price, String name) {
        this.price = price;
        this.name = name;
    }

    @Override
    public int compareTo(Product other) {
        return Double.compare(this.price, other.price);
    }

}
