package org.allmart.productservice.application.service;

import org.allmart.productservice.application.port.iin.ProductUseCase;
import org.allmart.productservice.application.port.out.ProductPersistencePort;
import org.allmart.productservice.domain.Product;

public class ProductService implements ProductUseCase {

    private final ProductPersistencePort productPersistencePort;

    public ProductService(ProductPersistencePort productPersistencePort) {
        this.productPersistencePort = productPersistencePort ;
    }

    @Override
    public Iterable<Product> getAllProducts() {
        return productPersistencePort.findAll();
    }

    @Override
    public Product getProductById(String productId) {
        return null;
    }
}
