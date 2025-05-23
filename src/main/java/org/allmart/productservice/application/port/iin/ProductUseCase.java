
package org.allmart.productservice.application.port.iin;

import org.allmart.productservice.domain.Product;

public interface ProductUseCase {
    Iterable<Product> getAllProducts();
    Product getProductById(String productId);

    Product registerProduct(Product product);

    Product decreaseStock(String productId, long quantity);

    Product increaseStock(String productId, long quantity);
}
