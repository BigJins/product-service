package allmart.productservice.adapter.webapi.dto;

import allmart.productservice.domain.product.Product;

public record ProductResponse(
        Long productId,
        Long categoryId,
        String categoryName,
        String name,
        String description,
        long price,
        String imageUrl,
        String status
) {
    public static ProductResponse of(Product p) {
        return new ProductResponse(
                p.getProductId(),
                p.getCategory().getCategoryId(),
                p.getCategory().getName(),
                p.getName(),
                p.getDescription(),
                p.getPrice(),
                p.getImageUrl(),
                p.getStatus().name()
        );
    }
}
