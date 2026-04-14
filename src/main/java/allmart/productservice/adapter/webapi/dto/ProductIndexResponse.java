package allmart.productservice.adapter.webapi.dto;

import allmart.productservice.domain.product.Product;

public record ProductIndexResponse(
        Long productId,
        String name,
        String description,
        long sellingPrice,
        Long categoryId,
        String categoryName,
        String unit,
        Integer unitSize,
        String status,
        String imageUrl
) {
    public static ProductIndexResponse of(Product p) {
        return new ProductIndexResponse(
                p.getProductId(),
                p.getName(),
                p.getDescription(),
                p.getSellingPrice(),
                p.getCategory().getCategoryId(),
                p.getCategory().getName(),
                p.getUnit(),
                p.getUnitSize(),
                p.getStatus().name(),
                p.getImageUrl()
        );
    }
}
