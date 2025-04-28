package org.allmart.productservice.application.port.out;

import org.allmart.productservice.domain.Product;

import java.util.Optional;

public interface ProductPersistencePort {
    Iterable<Product> findAll();
    Optional<Product> findByProductId(String productId);

    Product save(Product product);

    // 재고만 건들고 싶어서
    void updateStock(String productId, int stock);
}
