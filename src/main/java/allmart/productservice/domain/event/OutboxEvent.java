package allmart.productservice.domain.event;

import allmart.productservice.domain.AbstractEntity;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OutboxEvent extends AbstractEntity {

    private String eventType;    // product.registered.v1 | product.deleted.v1

    private String aggregateId;  // productId

    private String aggregateType;  // product

    private String payload;      // JSON 페이로드

    private LocalDateTime createdAt;

    public static OutboxEvent create(String eventType, String aggregateId, String payload) {
        OutboxEvent event = new OutboxEvent();
        event.eventType = eventType;
        event.aggregateId = aggregateId;
        event.aggregateType = "product";
        event.payload = payload;
        event.createdAt = LocalDateTime.now();
        return event;
    }
}
