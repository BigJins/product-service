package org.allmart.productservice.infrastructure.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;


/**
 * 자료구조: String
 */
@Service
@RequiredArgsConstructor
public class ProductStockRedisService {

    @Qualifier("stockRedisTemplate")
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String PRODUCT_STOCK_KEY = "product:stock:";

    // 재고 등록
    public void initStock(String productId, long stock) {
        redisTemplate.opsForValue().set(PRODUCT_STOCK_KEY + productId, stock);
    }

    // 재고 증가
    public Long increaseStock(String productId, long quantity) {
        return redisTemplate.opsForValue().increment(PRODUCT_STOCK_KEY + productId, quantity);
    }

    // 재고 감소
    public Long decreaseStock(String productId, long quantity) {
        Long result = redisTemplate.opsForValue().decrement(PRODUCT_STOCK_KEY + productId, quantity);
        if (result == null) throw new IllegalStateException("레디스 시스템 문제");
        if (result <= 0) throw new IllegalArgumentException("재고 부족, 상품 구매 불가");
        return result;
    }

    // 재고 조회
    public Long getStock(String productId) {
        Object stock = redisTemplate.opsForValue().get(PRODUCT_STOCK_KEY + productId);

        if (stock == null) throw new IllegalStateException("레디스에 상품 없음");

        if (stock instanceof Number) {
            return ((Number) stock).longValue(); // Integer, Long 모두 처리 가능
        }
        throw new IllegalStateException("잘못된 재고 데이터 타입");
    }

}
