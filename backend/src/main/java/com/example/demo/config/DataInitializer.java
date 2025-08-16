package com.example.demo.config;

import com.example.demo.entity.Product;
import com.example.demo.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    
    private final ProductRepository productRepository;
    
    @Override
    public void run(String... args) throws Exception {
        // Only initialize if no products exist
        if (productRepository.count() == 0) {
            initializeProducts();
        }
    }
    
    private void initializeProducts() {
        Product[] products = {
            new Product(null, "Laptop", "High-performance laptop with latest specifications", 
                new BigDecimal("999.99"), 50, "Electronics", null, null),
            new Product(null, "Smartphone", "Latest smartphone with advanced features", 
                new BigDecimal("699.99"), 100, "Electronics", null, null),
            new Product(null, "Headphones", "Wireless noise-canceling headphones", 
                new BigDecimal("199.99"), 75, "Electronics", null, null),
            new Product(null, "Coffee Maker", "Automatic coffee maker with timer", 
                new BigDecimal("89.99"), 30, "Home & Kitchen", null, null),
            new Product(null, "Running Shoes", "Comfortable running shoes for all terrains", 
                new BigDecimal("129.99"), 60, "Sports", null, null),
            new Product(null, "Backpack", "Durable backpack with multiple compartments", 
                new BigDecimal("59.99"), 40, "Fashion", null, null),
            new Product(null, "Watch", "Elegant wristwatch with leather strap", 
                new BigDecimal("299.99"), 25, "Fashion", null, null),
            new Product(null, "Blender", "High-speed blender for smoothies and shakes", 
                new BigDecimal("79.99"), 35, "Home & Kitchen", null, null)
        };
        
        productRepository.saveAll(Arrays.asList(products));
        System.out.println("Sample products initialized successfully!");
    }
}
