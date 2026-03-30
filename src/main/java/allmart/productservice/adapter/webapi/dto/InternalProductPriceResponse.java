package allmart.productservice.adapter.webapi.dto;

import allmart.productservice.domain.product.Product;

/**
 * order-service 내부 호출용 상품 가격 응답
 * GET /internal/products/{id}/price
 */
public record InternalProductPriceResponse(Long productId, String name, long price) {
    public static InternalProductPriceResponse of(Product p) {
        return new InternalProductPriceResponse(p.getProductId(), p.getName(), p.getPrice());
    }
}
