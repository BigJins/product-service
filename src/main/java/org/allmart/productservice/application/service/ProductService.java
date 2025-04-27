package org.allmart.productservice.application.service;

import org.allmart.productservice.application.port.iin.ProductUseCase;
import org.allmart.productservice.application.port.out.ProductPersistencePort;
import org.allmart.productservice.domain.Product;
import org.allmart.productservice.exception.ProductErrorCode;
import org.allmart.productservice.exception.ProductException;


public class ProductService implements ProductUseCase {

    private final ProductPersistencePort productPersistencePort;

    public ProductService(ProductPersistencePort productPersistencePort) {
        this.productPersistencePort = productPersistencePort;
    }

    @Override
    public Iterable<Product> getAllProducts() {
        return productPersistencePort.findAll();
    }

    @Override
    public Product getProductById(String productId) {

        return productPersistencePort.findByProductId(productId).orElseThrow(()
                -> new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND));
    }

    @Override
    public Product registerProduct(Product product) {
        product.validateForRegistration();

        productPersistencePort.findByProductId(product.getProductId()).ifPresent(existingProduct -> {
            throw new ProductException(ProductErrorCode.PRODUCT_ALREADY_EXISTS);
        });

        return productPersistencePort.save(product);
    }
}
