package org.allmart.productservice.presentation.mapper;

import org.allmart.productservice.domain.Product;
import org.allmart.productservice.presentation.dto.ProductRequestDTO;

public class ProductRequestMapper {
    public static Product toDomain(ProductRequestDTO dto) {
        return new Product.Builder(dto.getProductId(), dto.getProductName())
                .stock(dto.getStock())
                .unitPrice(dto.getUnitPrice())
                .build();
    }
}