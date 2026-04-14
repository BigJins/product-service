package allmart.productservice.adapter.webapi;

import allmart.productservice.adapter.webapi.dto.InternalProductPriceResponse;
import allmart.productservice.adapter.webapi.dto.ProductIndexResponse;
import allmart.productservice.application.provided.ProductFinder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 서비스 간 내부 호출 전용 API
 *
 * Gateway에서 /internal/** 경로를 외부 차단 (denyAll)
 * order-service: 주문 생성 시 상품 가격 검증
 * search-service: 초기 전체 상품 배치 색인
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

    /**
     * search-service 초기 배치 색인용 전체 상품 조회
     * DELETED 상태 제외, 페이징 처리 (기본 100개씩)
     */
    @GetMapping("/all")
    public Page<ProductIndexResponse> findAll(
            @PageableDefault(size = 100) Pageable pageable) {
        return productFinder.findAllForIndex(pageable).map(ProductIndexResponse::of);
    }

    /**
     * chat-service RAG 폴백용 키워드 검색
     * DELETED 상태 제외, 이름 부분 일치
     */
    @GetMapping("/search")
    public java.util.List<ProductIndexResponse> searchByKeyword(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "8") int size) {
        return productFinder.searchByKeyword(keyword, size).map(ProductIndexResponse::of).getContent();
    }
}
