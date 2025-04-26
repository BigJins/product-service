package org.allmart.productservice.application.service;

import org.allmart.productservice.application.port.iin.ProductUseCase;
import org.allmart.productservice.application.port.out.ProductPersistencePort;
import org.allmart.productservice.domain.Product;
import org.allmart.productservice.entity.ProductRepository;
import org.allmart.productservice.exception.ProductNotFoundException;
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

        // when & then

        assertThrows(ProductNotFoundException.class, () -> productService.getProductById(InvalidValue));


    }
}