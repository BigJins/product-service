package allmart.productservice.adapter.client;

import allmart.productservice.adapter.client.dto.InventoryInitRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * inventory-service 동기 HTTP 클라이언트.
 * 상품 등록 완료 후 초기 재고를 설정하기 위해 호출한다.
 */
@Component
@RequiredArgsConstructor
public class InventoryServiceClient {

    private final RestClient inventoryServiceRestClient;

    public void initialize(Long productId, int quantity) {
        inventoryServiceRestClient.post()
                .uri("/internal/inventory")
                .body(new InventoryInitRequest(productId, quantity))
                .retrieve()
                .toBodilessEntity();
    }
}
