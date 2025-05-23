package org.allmart.productservice.application.port.out;

import org.allmart.productservice.domain.Product;

import java.util.Optional;

public interface ProductPersistencePort {
    Iterable<Product> findAll();
    Optional<Product> findByProductId(String productId);

    Product save(Product product);

    void recordStockIncrease(String productId, long quantity);

    void recordStockDecrease(String productId, long quantity);
}
