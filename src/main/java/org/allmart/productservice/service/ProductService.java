package org.allmart.productservice.service;

import org.allmart.productservice.entity.ProductEntity;

public interface ProductService {
    Iterable<ProductEntity> getAllProducts();

    ProductEntity getProductById(String productId);
}
