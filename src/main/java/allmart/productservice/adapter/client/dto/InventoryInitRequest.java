package allmart.productservice.adapter.client.dto;

/**
 * inventory-service POST /internal/inventory 요청 DTO
 */
public record InventoryInitRequest(
        Long productId,
        int quantity
) {}
