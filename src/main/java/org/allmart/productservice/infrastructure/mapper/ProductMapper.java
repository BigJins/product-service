package org.allmart.productservice.infrastructure.mapper;

import org.allmart.productservice.domain.Product;
import org.allmart.productservice.infrastructure.entity.ProductEntity;

public class ProductMapper {

    // Entity -> Domain
    public static Product toDomain(ProductEntity entity) {
        return new Product.Builder(entity.getProductId(), entity.getProductName())
                .stock(entity.getStock())
                .unitPrice(entity.getUnitPrice())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    // Domain -> Entity
    public static ProductEntity toEntity(Product domain) {
        return ProductEntity.builder()
                .productId(domain.getProductId())
                .productName(domain.getProductName())
                .stock(domain.getStock())
                .unitPrice(domain.getUnitPrice())
                .createdAt(domain.getCreatedAt())
                .build();
    }
}