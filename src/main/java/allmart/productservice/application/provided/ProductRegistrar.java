package allmart.productservice.application.provided;

import allmart.productservice.domain.product.Product;
import org.springframework.web.multipart.MultipartFile;

public interface ProductRegistrar {
    Product register(Long categoryId, String name, String description, long sellingPrice, long purchasePrice, int initialQuantity, String status, MultipartFile image);
}
