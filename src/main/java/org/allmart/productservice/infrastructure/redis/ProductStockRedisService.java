package org.allmart.productservice.infrastructure.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Duration;


/**
 * 자료구조: String
 * 캐시 무효화 + TTL
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class ProductStockRedisService {

    @Qualifier("stockRedisTemplate")
    private final RedisTemplate<String, Long> redisTemplate;

    private static final String PRODUCT_STOCK_KEY = "product:stock:";

    // 재고 등록
    public void initStock(String productId, long stock) {
        log.info("[Redis] 초기 재고 설정: {} → {}", productId, stock);
        redisTemplate.opsForValue().set(PRODUCT_STOCK_KEY + productId, stock);
    }

    // 재고 증가
    public Long increaseStock(String productId, long quantity) {
        return redisTemplate.opsForValue().increment(PRODUCT_STOCK_KEY + productId, quantity);
    }

    // 재고 감소
//    public Long decreaseStock(String productId, long quantity) {
//        Long result = redisTemplate.opsForValue().decrement(PRODUCT_STOCK_KEY + productId, quantity);
//        if (result == null) throw new IllegalStateException("레디스 시스템 문제");
//        if (result <= 0) throw new IllegalArgumentException("재고 부족, 상품 구매 불가");
//        return result;
//    }

    public Long decreaseStockWithLua(String productId, long quantity) {
        String key = PRODUCT_STOCK_KEY + productId;

        String script = """
                    local stock = tonumber(redis.call('get', KEYS[1]))
                    if stock == nil then
                        return -1
                    end
                    if stock < tonumber(ARGV[1]) then
                        return -2
                    end
                    return redis.call('decrby', KEYS[1], ARGV[1])
                """;
        Object result = redisTemplate.execute(
                (RedisCallback<Object>) connection -> connection.scriptingCommands().eval(
                        script.getBytes(),
                        ReturnType.INTEGER,
                        1,
                        key.getBytes(),
                        String.valueOf(quantity).getBytes()
                )
        );

        if (result == null) throw new IllegalStateException("Redis Lua 실행 실패");

        Long res = (Long) result;
        log.info("[Redis] Lua 실행 결과 = {}", res);
        if (res == -1) throw new IllegalArgumentException("상품 없음");
        if (res == -2) throw new IllegalArgumentException("재고 부족");
        return res;
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
