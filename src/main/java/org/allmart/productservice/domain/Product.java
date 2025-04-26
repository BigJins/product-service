package org.allmart.productservice.domain;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class Product {

    private final String productId;
    private final String productName;
    private final Integer stock;
    private final BigDecimal unitPrice;
    private final LocalDateTime createdAt;
}
