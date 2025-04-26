package org.allmart.productservice.service;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.allmart.productservice.entity.ProductEntity;
import org.allmart.productservice.entity.ProductRepository;
import org.allmart.productservice.exception.ProductNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Builder
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Override
    public Iterable<ProductEntity> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    public ProductEntity getProductById(String productId) {
        return productRepository.findByProductId(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
    }
}
