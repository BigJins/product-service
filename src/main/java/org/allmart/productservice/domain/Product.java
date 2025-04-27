package org.allmart.productservice.domain;

import org.allmart.productservice.exception.ProductErrorCode;
import org.allmart.productservice.exception.ProductException;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Product {

    private final String productId;
    private final String productName;
    private final int stock;
    private final BigDecimal unitPrice;
    private final LocalDateTime createdAt;


    // 빌더패턴-점층적 생성자 패턴과 자바빈즈 패턴의 장점만
    public static class Builder {
        // 필수 매개변수
        private String productId;
        private String productName;

        // 선택 매개변수 - 기본값으로 초기화
        private int stock = 0;
        private BigDecimal unitPrice = BigDecimal.ZERO;
        private LocalDateTime createdAt = LocalDateTime.now();

        public Builder (String productId, String productName) {
            this.productId = productId;
            this.productName = productName;
        }

        public Builder stock(int val) {
            stock = val;
            return this;
        }

        public Builder unitPrice(BigDecimal val) {
            unitPrice = val;
            return this;
        }

        public Builder createdAt(LocalDateTime val) {
            createdAt = val;
            return this;
        }

        public Product build() {
            return new Product(this);
        }
    }

    // private 생성자: 외부에서는 Builder로만 생성 가능
    private Product(Builder builder) {
        this.productId = builder.productId;
        this.productName = builder.productName;
        this.stock = builder.stock;
        this.unitPrice = builder.unitPrice;
        this.createdAt = builder.createdAt;
    }

    // 각 필드 Getter 작성
    public String getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public int getStock() {
        return stock;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // 재고 감소
    public Product decreaseStock(int quantity) {
        if (this.stock - quantity < 0) throw new ProductException(ProductErrorCode.STOCK_SHORTAGE);
//        return this.toBuilder().stock(this.stock - quantity).build();
        return new Builder(productId, productName)
                .stock(stock - quantity)
                .createdAt(createdAt)
                .build();
    }

    // 재고 증가
    public Product increaseStock(int quantity) {
        if (quantity <= 0) throw new ProductException(ProductErrorCode.INVALID_STOCK_QUANTITY);
//        return this.toBuilder().stock(this.stock + quantity).build();
        return new Builder(productId, productName)
                .stock(stock + quantity)
                .createdAt(createdAt)
                .build();

    }


    // 판매 가능 여부
    public boolean isAvailable() {
        return this.stock > 0;
    }

    // 상품은 비면 안돼 (리팩토링 대상)
    public void validateForRegistration() {
        if (this.getProductId() == null || this.getProductId().isBlank() ||
                this.getProductName() == null || this.getProductName().isBlank()) {
            throw new ProductException(ProductErrorCode.PRODUCT_NAME_EMPTY);
        }
    }
}
