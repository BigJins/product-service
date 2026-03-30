package allmart.productservice.application.provided;

import allmart.productservice.domain.product.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductFinder {
    Product findById(Long productId);
    Page<Product> findAll(Long categoryId, Pageable pageable);
}
