package org.allmart.productservice.infrastructure.adapter;

import org.allmart.productservice.domain.Product;
import org.allmart.productservice.infrastructure.entity.ProductEntity;
import org.allmart.productservice.infrastructure.mapper.ProductMapper;
import org.allmart.productservice.infrastructure.redis.ProductStockRedisService;
import org.allmart.productservice.infrastructure.repo.ProductJPARepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


class ProductPersistenceAdapterTest {

    private ProductPersistenceAdapter adapter;
    private ProductJPARepository repository;
    private ProductStockRedisService redisService;

    @BeforeEach
    void setUp() {
        repository = mock(ProductJPARepository.class);
        redisService = mock(ProductStockRedisService.class);
        adapter = new ProductPersistenceAdapter(repository, redisService);

    }

    @Test
    void 상품을_저장할_수_있다() {
        // given
        Product product = new Product.Builder("testId","테스트 상품")
                .stock(100)
                .unitPrice(BigDecimal.valueOf(1000))
                .createdAt(LocalDateTime.now())
                .build();

        ProductEntity entity = ProductMapper.toEntity(product);

        when(repository.save(any(ProductEntity.class))).thenReturn(entity);

        // when
        Product savedProduct = adapter.save(product);

        // then
        assertEquals(product.getProductId(), savedProduct.getProductId());
    }

    @Test
    void 상품을_레디스로_저장() {
        // given
        String productId = "testId";
        int stock = 100;

        Product product = new Product.Builder(productId,"테스트 상품")
                .stock(stock)
                .unitPrice(BigDecimal.valueOf(1000))
                .createdAt(LocalDateTime.now())
                .build();

        doNothing().when(redisService).initStock(productId, stock);

        ProductEntity entity = ProductMapper.toEntity(product);
        when(repository.save(any(ProductEntity.class))).thenReturn(entity);

        // when
        adapter.save(product);

        // them
        verify(redisService, times(1)).initStock(productId, stock);

    }

    @Test
    void 상품Id로_조회() {
        // given
        ProductEntity entity = ProductEntity.builder()
                .productId("testId")
                .productName("테스트 상품")
                .stock(100)
                .unitPrice(BigDecimal.valueOf(10000))
                .createdAt(LocalDateTime.now())
                .build();

        when(repository.findByProductId("testId")).thenReturn(Optional.of(entity));

        // when
        Optional<Product> foundProduct = adapter.findByProductId("testId");

        // then
        assertTrue(foundProduct.isPresent());
        assertEquals(entity.getProductId(), foundProduct.get().getProductId());

    }

    @Test
    void 상품Id로_조회시_레디스_재고확인() {
        // given
        String productId = "testId";

        ProductEntity entity = ProductEntity.builder()
                .productId(productId)
                .stock(100)
                .unitPrice(BigDecimal.valueOf(1000))
                .createdAt(LocalDateTime.now())
                .build();
        when(repository.findByProductId(productId)).thenReturn(Optional.of(entity));
        when(redisService.getStock(productId)).thenReturn(95L);

        // when
        Optional<Product> foundProduct = adapter.findByProductId(productId);

        // then
        assertThat(foundProduct).isPresent();
        assertThat(foundProduct.get().getStock()).isEqualTo(95);

        verify(repository, times(1)).findByProductId(productId);
        verify(redisService, times(1)).getStock(productId);
    }


    @Test
    void 전체_상품_조회() {
        // given
        when(repository.findAll()).thenReturn(Collections.emptyList());

        // when
        Iterable<Product> products = adapter.findAll();

        // then
        assertNotNull(products);
    }

    @Test
    void 전체_상품_조회시_각_상품의_재고는_Redis_기준() {
        // given
        ProductEntity entity1 = ProductEntity.builder()
                .productId("product1")
                .productName("상품1")
                .stock(100) // DB에 저장된 초기값
                .unitPrice(BigDecimal.valueOf(5000))
                .createdAt(LocalDateTime.now())
                .build();

        ProductEntity entity2 = ProductEntity.builder()
                .productId("product2")
                .productName("상품2")
                .stock(200)
                .unitPrice(BigDecimal.valueOf(10000))
                .createdAt(LocalDateTime.now())
                .build();

        when(repository.findAll()).thenReturn(List.of(entity1, entity2));
        when(redisService.getStock(entity1.getProductId())).thenReturn(100L);
        when(redisService.getStock(entity2.getProductId())).thenReturn(200L);

        // when
        Iterable<Product> products = adapter.findAll();

        // then
        List<Product> productList = (List<Product>) products;
        assertThat(productList).hasSize(2);

        Product p1 = productList.get(0);
        Product p2 = productList.get(1);

        assertThat(p1.getProductId()).isEqualTo("product1");
        assertThat(p1.getStock()).isEqualTo(100);
        assertThat(p2.getProductId()).isEqualTo("product2");
        assertThat(p2.getStock()).isEqualTo(200);

        verify(redisService,times(1)).getStock(entity1.getProductId());
        verify(redisService, times(1)).getStock(entity2.getProductId());

    }

    @Test
    void 재고_증가() {
        // when
        adapter.increaseStock("testId",100);

        // then
        verify(repository, times(1)).increaseStock("testId",100);
    }

    @Test
    void 재고_증가_레디스와_DB호출() {
        // given
        String productId = "testId";
        int quantity = 3;

        when(redisService.increaseStock(productId, quantity)).thenReturn(8L); // 레디스 재고 5 가정 -> 5 + 3 = 8

        // when
        adapter.increaseStock(productId, quantity);

        // then
        verify(redisService, times(1)).increaseStock(productId, quantity);
        verify(repository, times(1)).increaseStock(productId, quantity);
    }

    @Test
    void 재고_감소() {
        // when
        adapter.decreaseStock("testId",100);

        // then
        verify(repository, times(1)).decreaseStock("testId",100);
    }

    @Test
    void 재고_감소_레디스와_DB호출() {
        // given
        String productId = "testId";
        int quantity = 3;

        when(redisService.increaseStock(productId, quantity)).thenReturn(2L); // 레디스 재고 5 가정 -> 5 - 3 = 2

        // when
        adapter.decreaseStock(productId, quantity);

        // then
        verify(redisService, times(1)).decreaseStock(productId, quantity);
        verify(repository, times(1)).decreaseStock(productId, quantity);
    }
}