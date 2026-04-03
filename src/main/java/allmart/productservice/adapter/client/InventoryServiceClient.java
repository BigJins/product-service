package allmart.productservice.adapter.client;

import allmart.productservice.adapter.client.dto.InventoryInitRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * inventory-service 동기 HTTP 클라이언트.
 * 상품 등록 완료 후 초기 재고를 설정하기 위해 호출한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryServiceClient {

    private final RestClient inventoryServiceRestClient;

    public void initialize(Long productId, int quantity) {
        log.info("inventory-service 재고 초기화 요청: productId={}, quantity={}", productId, quantity);
        try {
            inventoryServiceRestClient.post()
                    .uri("/internal/inventory")
                    .body(new InventoryInitRequest(productId, quantity))
                    .retrieve()
                    .toBodilessEntity();
            log.info("inventory-service 재고 초기화 완료: productId={}, quantity={}", productId, quantity);
        } catch (Exception e) {
            log.error("inventory-service 재고 초기화 실패: productId={}, quantity={}, error={}", productId, quantity, e.getMessage(), e);
            throw e;
        }
    }
}
