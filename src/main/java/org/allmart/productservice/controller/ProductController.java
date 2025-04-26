package org.allmart.productservice.controller;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.allmart.productservice.entity.ProductEntity;
import org.allmart.productservice.service.ProductService;
import org.allmart.productservice.vo.ResponseProduct;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/product-service")
@RequiredArgsConstructor
@Builder
public class ProductController {

    private final Environment env;
    private final ProductService productService;

    @GetMapping("/health_check")
    public String status() {
        return String.format("It's Working! (===== Product Service PORT: %s =====)", env.getProperty("local.server.port"));
    }

    @GetMapping("/products")
    public ResponseEntity<List<ResponseProduct>> getProducts() {
        Iterable<ProductEntity> productList = productService.getAllProducts();

        List<ResponseProduct> result = new ArrayList<>();

        productList.forEach(entity -> {
            result.add(ResponseProduct.builder()
                    .productId(entity.getProductId())
                    .productName(entity.getProductName())
                    .unitPrice(entity.getUnitPrice())
                    .stock(entity.getStock())
                    .createdAt(entity.getCreatedAt())
                    .build()
            );
        });

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(result);
    }


    // 단일 상품 조회
    @GetMapping("/products/{productId}")
    public ResponseEntity<ResponseProduct> getProduct(
            @PathVariable String productId) {
        ProductEntity entity = productService.getProductById(productId);
        ResponseProduct dto = ResponseProduct.builder()
                .productId(entity.getProductId())
                .productName(entity.getProductName())
                .unitPrice(entity.getUnitPrice())
                .stock(entity.getStock())
                .createdAt(entity.getCreatedAt())
                .build();
        return ResponseEntity.ok(dto);
    }
}
