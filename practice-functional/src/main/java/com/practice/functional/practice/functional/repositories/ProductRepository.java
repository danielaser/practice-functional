package com.practice.functional.practice.functional.repositories;

import com.practice.functional.practice.functional.models.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
