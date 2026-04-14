package allmart.productservice.application.required;

import allmart.productservice.domain.product.Product;
import allmart.productservice.domain.product.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.Repository;

import java.util.Optional;

public interface ProductRepository extends Repository<Product, Long> {
    Product save(Product product);
    Optional<Product> findById(Long productId);
    Page<Product> findByCategory_CategoryIdAndStatus(Long categoryId, ProductStatus status, Pageable pageable);
    Page<Product> findByStatus(ProductStatus status, Pageable pageable);
    Page<Product> findByStatusNot(ProductStatus status, Pageable pageable);
    Page<Product> findByNameContainingIgnoreCaseAndStatusNot(String keyword, ProductStatus status, Pageable pageable);
    Page<Product> findByNameContainingIgnoreCaseAndStatus(String keyword, ProductStatus status, Pageable pageable);
}
