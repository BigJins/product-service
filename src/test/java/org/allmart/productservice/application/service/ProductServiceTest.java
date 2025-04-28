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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


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
        Product product1 = new Product.Builder("dnf_190", "얼음골 사과")
                .stock(100)
                .unitPrice(BigDecimal.valueOf(1000))
                .createdAt(LocalDateTime.now())
                .build();

        Product product2 = new Product.Builder("dnf_179", "얼음골 배")
                .stock(100)
                .unitPrice(BigDecimal.valueOf(2000))
                .createdAt(LocalDateTime.now())
                .build();

        when(productPersistencePort.findAll()).thenReturn(Arrays.asList(product1, product2));

        // when
        Iterable<Product> products = productService.getAllProducts();

        // then
        List<Product> productList = (List<Product>) products;
        assertEquals(2, productList.size());


    }

    @Test
    void 상품이_없으면_빈리스트() {
        // given
        when(productPersistencePort.findAll()).thenReturn(Collections.emptyList());

        // when
        Iterable<Product> products = productService.getAllProducts();

        // then
        assertNotNull(products);
        assertFalse(products.iterator().hasNext());
    }

    @Test
    void 상품ID로_조회() {
        // given
        Product product1 = new Product.Builder("dnf_190", "얼음골 사과")
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
        Product newProduct = new Product.Builder("dnf_195", "대구는 사과")
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
    void 같은_상품ID_가_중복이면_등록_실패_테스트() {

        // given
        String existingProductId = "dnf_195";

        Product existingProduct = new Product.Builder(existingProductId, "대구는 사과")
                .stock(100)
                .unitPrice(BigDecimal.valueOf(3000))
                .createdAt(LocalDateTime.now())
                .build();

        when(productPersistencePort.findByProductId(existingProductId)).thenReturn(Optional.of(existingProduct));

        Product product2 = new Product.Builder(existingProductId, "얼음골 배")
                .stock(100)
                .unitPrice(BigDecimal.valueOf(2000))
                .createdAt(LocalDateTime.now())
                .build();
        // when
        ProductException exception = assertThrows(ProductException.class,() -> productService.registerProduct(product2));

        // then
        assertEquals(ProductErrorCode.PRODUCT_ALREADY_EXISTS, exception.getErrorCode());

    }


    @Test
    void 상품찾고_재고를_줄일_수_있다() {
        // given
        Product product1 = new Product.Builder("dnf_190", "얼음골 사과")
                .stock(100)
                .unitPrice(BigDecimal.valueOf(1000))
                .createdAt(LocalDateTime.now())
                .build();

        when(productPersistencePort.findByProductId("dnf_190")).thenReturn(Optional.of(product1));

        // when
        Product updated = productService.decreaseStock("dnf_190", 30);

        // then
        assertEquals(70, updated.getStock());

        // updateStock 사용 검증
        verify(productPersistencePort).updateStock("dnf_190", 30);
    }

    @Test
    void 상품찾고_재고를_늘릴_수_있다() {
        // given
        Product product1 = new Product.Builder("dnf_190", "얼음골 사과")
                .stock(100)
                .unitPrice(BigDecimal.valueOf(1000))
                .createdAt(LocalDateTime.now())
                .build();

        when(productPersistencePort.findByProductId("dnf_190")).thenReturn(Optional.of(product1));

        // when
        Product updated = productService.increaseStock("dnf_190", 30);

        // then
        assertEquals(130, updated.getStock());

        // updateStock 사용 검증
        verify(productPersistencePort).updateStock("dnf_190", 30);
    }


}