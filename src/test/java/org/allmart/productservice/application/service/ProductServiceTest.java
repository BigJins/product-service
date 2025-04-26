package org.allmart.productservice.application.service;

import org.allmart.productservice.application.port.out.ProductPersistencePort;
import org.allmart.productservice.domain.Product;
import org.allmart.productservice.exception.ProductErrorCode;
import org.allmart.productservice.exception.ProductException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


class ProductServiceTest {


    private ProductPersistencePort productPersistencePort;
    private ProductService productService;

    @BeforeEach
    void setUp() {
        productPersistencePort = mock(ProductPersistencePort.class);
        productService = new ProductService(productPersistencePort);
    }


    @Test
    void 상품전체_조회() {

        // given
        Product product1 = Product.builder()
                .productId("dnf_190")
                .productName("얼음골 사과")
                .stock(100)
                .unitPrice(BigDecimal.valueOf(1000))
                .createdAt(LocalDateTime.now())
                .build();
        Product product2 = Product.builder()
                .productId("dnf_179")
                .productName("얼음골 배")
                .stock(100)
                .unitPrice(BigDecimal.valueOf(2000))
                .createdAt(LocalDateTime.now())
                .build();

        // when
        when(productPersistencePort.findAll()).thenReturn(Arrays.asList(product1, product2));


        Iterable<Product> products = productService.getAllProducts();

        // then
        List<Product> productList = (List<Product>) products;
        assertEquals(2, productList.size());


    }

    @Test
    void 상품ID로_조회() {
        // given
        Product product1 = Product.builder()
                .productId("dnf_190")
                .productName("얼음골 사과")
                .stock(100)
                .unitPrice(BigDecimal.valueOf(1000))
                .createdAt(LocalDateTime.now())
                .build();

        // when
        when(productPersistencePort.findByProductId("dnf_190")).thenReturn(Optional.of(product1));

        Product foundProduct = productService.getProductById("dnf_190");

        // then
        assertNotNull(foundProduct);
        assertEquals("dnf_190", foundProduct.getProductId());
        assertEquals("얼음골 사과", foundProduct.getProductName());
    }

    @Test
    void Null이면_ProductNotFoundException_을_보여준다() {
        // given
        String InvalidValue = "dnf_999";

        when(productPersistencePort.findByProductId(InvalidValue)).thenReturn(Optional.empty());

        // when
        ProductException exception = assertThrows(ProductException.class, () -> productService.getProductById(InvalidValue));

        // then
        assertEquals(ProductErrorCode.PRODUCT_NOT_FOUND, exception.getErrorCode());

    }


    @Test
    void 신상품을_등록해보자() {
        // given
        Product newProduct = Product.builder()
                .productId("dnf_195")
                .productName("대구는 사과")
                .stock(100)
                .unitPrice(BigDecimal.valueOf(3000))
                .createdAt(LocalDateTime.now())
                .build();

        when(productPersistencePort.save(newProduct)).thenReturn(newProduct);

        // when
        Product savedProduct = productService.registerProduct(newProduct);

        // then
        assertNotNull(savedProduct);
        assertEquals("dnf_195", savedProduct.getProductId());
        assertEquals("대구는 사과", savedProduct.getProductName());
    }

    @Test
    void 상품명이_빈_값이면_등록_실패_테스트() {
        // given
        Product newProduct = Product.builder()
                .productId("dnf_195")
                .productName("")// 공란
                .stock(100)
                .unitPrice(BigDecimal.valueOf(3000))
                .createdAt(LocalDateTime.now())
                .build();

        // when
        ProductException exception = assertThrows(ProductException.class, () -> productService.registerProduct(newProduct));

        // then
        assertEquals(ProductErrorCode.PRODUCT_NAME_EMPTY, exception.getErrorCode());
    }

    @Test
    void 같은_상품ID_가_중복이면_등록_실패_테스트() {

        // given
        String existingProductId = "dnf_195";

        Product existingProduct = Product.builder()
                .productId(existingProductId)
                .productName("대구는 사과")
                .stock(100)
                .unitPrice(BigDecimal.valueOf(3000))
                .createdAt(LocalDateTime.now())
                .build();

        when(productPersistencePort.findByProductId(existingProductId)).thenReturn(Optional.of(existingProduct));


        Product product2 = Product.builder()
                .productId(existingProductId)
                .productName("얼음골 배")
                .stock(100)
                .unitPrice(BigDecimal.valueOf(2000))
                .createdAt(LocalDateTime.now())
                .build();

        // when
        ProductException exception = assertThrows(ProductException.class,() -> productService.registerProduct(product2));

        // then
        assertEquals(ProductErrorCode.PRODUCT_ALREADY_EXISTS, exception.getErrorCode());

    }
}