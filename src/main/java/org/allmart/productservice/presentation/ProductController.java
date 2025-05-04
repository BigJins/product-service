package org.allmart.productservice.presentation;

import lombok.RequiredArgsConstructor;
import org.allmart.productservice.application.port.iin.ProductUseCase;
import org.allmart.productservice.domain.Product;
import org.allmart.productservice.presentation.dto.ProductRequestDTO;
import org.allmart.productservice.presentation.mapper.ProductRequestMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@Validated
@RestController
@RequestMapping("/product-service/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductUseCase productUseCase;

    @GetMapping("/all")
    public ResponseEntity<Iterable<Product>> getAllProducts() {
        return ResponseEntity.ok(productUseCase.getAllProducts());
    }

    @GetMapping("/{productId}")
    public ResponseEntity<Product> getProductById(@PathVariable String productId) {
        return ResponseEntity.ok(productUseCase.getProductById(productId));
    }

    @PostMapping("/register")
    public ResponseEntity<Product> registerProduct(@RequestBody ProductRequestDTO dto) {
        Product toSave = ProductRequestMapper.toDomain(dto);

        Product savedProduct = productUseCase.registerProduct(toSave);
        return ResponseEntity
                .created(URI.create("/product-service/products/" + savedProduct.getProductId()))
                .body(savedProduct);
    }

    @PostMapping("/{productId}/stock/decrease")
    public ResponseEntity<Void> decreaseStock(@PathVariable String productId, @RequestBody long quantity) {
        productUseCase.decreaseStock(productId, quantity);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{productId}/stock/increase")
    public ResponseEntity<Void> increaseStock(@PathVariable String productId, @RequestParam long quantity) {
        productUseCase.increaseStock(productId, quantity);
        return ResponseEntity.ok().build();
    }

}
