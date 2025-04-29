package org.allmart.productservice.infrastructure.redis;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * 자료구조: String
 */
class ProductStockRedisServiceTest {

    private ProductStockRedisService service;
    private RedisTemplate<String, Object> redisTemplate;
    private ValueOperations<String, Object> valueOperations;

    @BeforeEach
    void setUp() {
        redisTemplate = Mockito.mock(RedisTemplate.class);
        valueOperations = Mockito.mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        service = new ProductStockRedisService(redisTemplate);
    }

    @Test
    void 재고_등록() {
        // given
        String productId = "test_19";
        long stock = 20;

        // when
        service.initStock(productId, stock);

        // then
        verify(valueOperations, times(1))
                .set(eq("product:stock:" + productId), eq(stock));
    }

    @Test
    void 재고_증가() {
        // given
        String productId = "test_20";
        long quantity = 20L;
        when(valueOperations.increment("product:stock:" + productId, quantity)).thenReturn(120L);

        // when
        Long increaseStock = service.increaseStock(productId, quantity);

        // then
        verify(valueOperations, times(1))
                .increment(eq("product:stock:" + productId),eq(quantity));
        assertThat(increaseStock).isEqualTo(120L);
    }

    @Test
    void 재고_감소() {
        // given
        String productId = "test_20";
        long quantity = 20L;
        when(valueOperations.decrement("product:stock:" + productId, quantity)).thenReturn(80L);

        // when
        Long decreaseStock = service.decreaseStock(productId, quantity);

        // then
        verify(valueOperations, times(1))
                .decrement(eq("product:stock:" + productId),eq(quantity));
        assertThat(decreaseStock).isEqualTo(80L);
    }


    @Test
    void 재고조회_정상_Long타입() {
        // given
        String productId = "test_21";
        when(valueOperations.get("product:stock:" + productId)).thenReturn(100L);

        // when
        Long stock = service.getStock(productId);

        // then
        assertThat(stock).isEqualTo(100L);
    }

    @Test
    void 재고조회_정상_Integer타입() {
        // given
        String productId = "test_21";
        when(valueOperations.get("product:stock:" + productId)).thenReturn(100); // Integer로 반환

        // when
        Long stock = service.getStock(productId);

        // then
        assertThat(stock).isEqualTo(100L);
    }


    @Test
    void 재고_부족() {
        // given
        String productId = "test_22";
        long quantity = 20L;
        when(valueOperations.decrement("product:stock:" + productId, quantity)).thenReturn(0L);

        // when & then
        assertThatThrownBy(() -> service.decreaseStock(productId, quantity))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("재고 부족, 상품 구매 불가");
    }

    @Test
    void 재고감소_null() {
        // given
        String productId = "test_23";
        long quantity = 20L;
        when(valueOperations.decrement("product:stock:" + productId, quantity)).thenReturn(null);

        // when & then
        assertThatThrownBy(() -> service.decreaseStock(productId, quantity))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("레디스 시스템 문제");
    }

    @Test
    void 재고조회_null() {
        // given
        String productId = "test_21";
        when(valueOperations.get("product:stock:" + productId)).thenReturn(null);

        // when & then
        assertThatThrownBy(() -> service.getStock(productId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("레디스에 상품 없음");
    }

    @Test
    void 재고조회_잘못된타입_에러() {
        // given
        String productId = "product_004";
        when(valueOperations.get("product:stock:" + productId)).thenReturn("NotANumber");

        // when & then
        assertThatThrownBy(() -> service.getStock(productId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("잘못된 재고 데이터 타입");
    }
}