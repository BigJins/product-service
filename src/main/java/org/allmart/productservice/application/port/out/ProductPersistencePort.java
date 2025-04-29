package org.allmart.productservice.application.port.out;

import org.allmart.productservice.domain.Product;

import java.util.Optional;

public interface ProductPersistencePort {
    Iterable<Product> findAll();
    Optional<Product> findByProductId(String productId);

    Product save(Product product);

    void increaseStock(String productId, int amount);

    void decreaseStock(String productId, int amount);
}
