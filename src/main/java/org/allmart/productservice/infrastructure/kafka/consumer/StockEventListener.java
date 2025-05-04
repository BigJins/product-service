package org.allmart.productservice.infrastructure.kafka.consumer;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.allmart.productservice.application.port.out.ProductPersistencePort;
import org.allmart.productservice.infrastructure.kafka.event.StockDecreasedEvent;
import org.allmart.productservice.infrastructure.redis.ProductStockRedisService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;


@Log4j2
@Component
@RequiredArgsConstructor
@Transactional
public class StockEventListener {

    private final ProductStockRedisService redisService;
    private final ProductPersistencePort productPersistencePort;

    @KafkaListener(
            topics = "product.stock.events",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleEvent(StockDecreasedEvent event) {
        log.info("[Kafka] 수신된 이벤트: {}", event);
        log.info("[Kafka] 실제 타입: {}", event.getClass());

        try {
            redisService.decreaseStockWithLua(event.getProductId(), event.getQuantity());
            productPersistencePort.recordStockDecrease(event.getProductId(), event.getQuantity());
            log.info("재고 감소 성공: {}", event);
        } catch (Exception e) {
            log.error("재고 감소 처리 실패: {}", e.getMessage());
            // TODO: DLQ 또는 재시도 큐에 넣기
        }
    }
}