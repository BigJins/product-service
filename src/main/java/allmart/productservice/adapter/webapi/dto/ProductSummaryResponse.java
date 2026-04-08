package allmart.productservice.adapter.webapi.dto;

import allmart.productservice.domain.product.Product;

import java.time.LocalDateTime;

public record ProductSummaryResponse(
        Long productId,
        String name,
        long sellingPrice,
        String taxType,
        String imageUrl,
        Long categoryId,
        String categoryName,
        String status,
        LocalDateTime createdAt
) {
    public static ProductSummaryResponse of(Product p) {
        return new ProductSummaryResponse(
                p.getProductId(),
                p.getName(),
                p.getSellingPrice(),
                p.getTaxType().name(),
                p.getImageUrl(),
                p.getCategory().getCategoryId(),
                p.getCategory().getName(),
                p.getStatus().name(),
                p.getCreatedAt()
        );
    }
}
