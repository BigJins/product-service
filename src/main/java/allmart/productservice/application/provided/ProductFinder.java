package allmart.productservice.application.provided;

import allmart.productservice.domain.product.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductFinder {
    Product findById(Long productId);
    Page<Product> findAll(Long categoryId, Pageable pageable);
    Page<Product> findAllForIndex(Pageable pageable);  // search-service 초기 배치 색인용
    Page<Product> searchByKeyword(String keyword, int size);  // chat-service 키워드 검색 폴백
}
