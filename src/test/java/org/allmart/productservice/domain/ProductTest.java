package org.allmart.productservice.domain;

import org.allmart.productservice.exception.ProductErrorCode;
import org.allmart.productservice.exception.ProductException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ProductTest {

    @Test
    void 재고가_감소한다() {
        // given
        Product product = new Product.Builder("gotksanf_1","문어")
                .stock(100)
                .unitPrice(BigDecimal.valueOf(2000))
                .createdAt(LocalDateTime.now())
                .build();

        // when
        Product updateProduct = product.decreaseStock(2);
        
        // then
        assertEquals(98, updateProduct.getStock());
    }


    @Test
    void 재고_부족() {
        // given
        Product product = new Product.Builder("gotksanf_1","문어")
                .stock(100)
                .unitPrice(BigDecimal.valueOf(2000))
                .createdAt(LocalDateTime.now())
                .build();


        // when
        ProductException exception = assertThrows(ProductException.class, () -> product.decreaseStock(101));

        // then
        assertEquals(ProductErrorCode.STOCK_SHORTAGE,exception.getErrorCode());
    }
    @Test
    void 재고를_증가시킴() {
        // given
//        Product product = Product.builder()
//                .productId("gotksanf_1")
//                .productName("문어")
//                .stock(100)
//                .unitPrice(BigDecimal.valueOf(2000))
//                .createdAt(LocalDateTime.now())
//                .build();
        Product product = new Product.Builder("gotksanf_1","문어")
                .stock(100)
                .unitPrice(BigDecimal.valueOf(2000))
                .createdAt(LocalDateTime.now())
                .build();


        // when
        Product updateProduct = product.increaseStock(3);

        // then
        assertEquals(103, updateProduct.getStock());
    }

    @Test
    void 재고_증가시_0이하는_입력x() {
        // given
        Product product = new Product.Builder("gotksanf_1","문어")
                .stock(100)
                .unitPrice(BigDecimal.valueOf(2000))
                .createdAt(LocalDateTime.now())
                .build();

        // when
        ProductException exception = assertThrows(ProductException.class, () -> product.increaseStock(-2));

        // then
        assertEquals(ProductErrorCode.INVALID_STOCK_QUANTITY,exception.getErrorCode());
    }

    @Test
    void 재고O_판매_가능(){
        // given
        Product product = new Product.Builder("gotksanf_1","문어")
                .stock(100)
                .unitPrice(BigDecimal.valueOf(2000))
                .createdAt(LocalDateTime.now())
                .build();



        // then
        assertTrue(product.isAvailable());
    }

    @Test
    void 재고X_판매_불가능() {

        // given
        Product product = new Product.Builder("gotksanf_1","문어")
                .stock(0)
                .unitPrice(BigDecimal.valueOf(2000))
                .createdAt(LocalDateTime.now())
                .build();


        // then
        assertFalse(product.isAvailable());
    }

    @Test
    void 상품명이_빈_값이면_등록_실패_테스트() {
        // given
        Product product = new Product.Builder("gotksanf_1","")
                .stock(100)
                .unitPrice(BigDecimal.valueOf(2000))
                .createdAt(LocalDateTime.now())
                .build();

        // when
        ProductException exception = assertThrows(ProductException.class, product::validateForRegistration);

        // then
        assertEquals(ProductErrorCode.PRODUCT_NAME_EMPTY, exception.getErrorCode());
    }

    @Test
    void 상품ID가_빈_값이면_등록_실패_테스트() {
        // given
        Product product = new Product.Builder("","문어")
                .stock(100)
                .unitPrice(BigDecimal.valueOf(2000))
                .createdAt(LocalDateTime.now())
                .build();


        // when
        ProductException exception = assertThrows(ProductException.class, product::validateForRegistration);
        // then
        assertEquals(ProductErrorCode.PRODUCT_NAME_EMPTY, exception.getErrorCode());
    }

    @Test
    void 상품ID가_null_값이면_등록_실패_테스트() {
        // given
        Product product = new Product.Builder(null,"문어")
                .stock(100)
                .unitPrice(BigDecimal.valueOf(2000))
                .createdAt(LocalDateTime.now())
                .build();


        // when
        ProductException exception = assertThrows(ProductException.class, product::validateForRegistration);
        // then
        assertEquals(ProductErrorCode.PRODUCT_NAME_EMPTY, exception.getErrorCode());
    }

    @Test
    void 상품명이_null_값이면_등록_실패_테스트() {
        // given
        Product product = new Product.Builder("gotksanf_1",null)
                .stock(100)
                .unitPrice(BigDecimal.valueOf(2000))
                .createdAt(LocalDateTime.now())
                .build();


        // when
        ProductException exception = assertThrows(ProductException.class, product::validateForRegistration);

        // then
        assertEquals(ProductErrorCode.PRODUCT_NAME_EMPTY, exception.getErrorCode());
    }


    @Test
    void 정보유실방지() {

        LocalDateTime createdTime = LocalDateTime.now(); // 비교를 위해 따로 저장
        Product product = new Product.Builder("gotksanf_1", "테스트상품")
                .stock(100)
                .unitPrice(BigDecimal.valueOf(2000))
                .createdAt(createdTime)
                .build();

        // when
        Product decreasedProduct = new Product.Builder(product.getProductId(), product.getProductName())
                .stock(product.getStock() - 10)
                .unitPrice(product.getUnitPrice())
                .createdAt(product.getCreatedAt())
                .build();

        // then
        assertEquals("gotksanf_1", decreasedProduct.getProductId());
        assertEquals("테스트상품", decreasedProduct.getProductName());
        assertEquals(90, decreasedProduct.getStock());
        assertEquals(BigDecimal.valueOf(2000), decreasedProduct.getUnitPrice());
        assertEquals(createdTime, decreasedProduct.getCreatedAt());
    }
}