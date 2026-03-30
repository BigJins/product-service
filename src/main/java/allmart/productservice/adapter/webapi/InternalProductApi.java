package allmart.productservice.adapter.webapi;

import allmart.productservice.adapter.webapi.dto.InternalProductPriceResponse;
import allmart.productservice.application.provided.ProductFinder;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 서비스 간 내부 호출 전용 API
 *
 * Gateway에서 /internal/** 경로를 외부 차단 (denyAll)
 * order-service가 주문 생성 시 상품 가격 검증에 사용
 */
@RestController
@RequestMapping("/internal/products")
@RequiredArgsConstructor
public class InternalProductApi {

    private final ProductFinder productFinder;

    @GetMapping("/{productId}/price")
    public InternalProductPriceResponse getPrice(@PathVariable Long productId) {
        return InternalProductPriceResponse.of(productFinder.findById(productId));
    }
}
