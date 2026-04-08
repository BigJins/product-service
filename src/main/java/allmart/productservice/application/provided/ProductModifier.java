package allmart.productservice.application.provided;

import allmart.productservice.domain.product.Product;
import org.springframework.web.multipart.MultipartFile;

public interface ProductModifier {
    Product update(Long productId, String name, String description, Long sellingPrice, Long purchasePrice, Long categoryId, String status, MultipartFile image);
    void delete(Long productId);
}
