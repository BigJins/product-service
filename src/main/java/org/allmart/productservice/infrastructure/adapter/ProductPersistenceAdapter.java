package org.allmart.productservice.infrastructure.adapter;

import lombok.extern.log4j.Log4j2;
import org.allmart.productservice.application.port.out.ProductPersistencePort;
import org.allmart.productservice.domain.Product;
import org.allmart.productservice.infrastructure.entity.ProductEntity;
import org.allmart.productservice.infrastructure.entity.ProductStockLogEntity;
import org.allmart.productservice.infrastructure.mapper.ProductMapper;
import org.allmart.productservice.infrastructure.redis.ProductStockRedisService;
import org.allmart.productservice.infrastructure.repo.ProductJPARepository;
import org.allmart.productservice.infrastructure.repo.ProductStockLogRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Log4j2
@Component
@Transactional
public class ProductPersistenceAdapter implements ProductPersistencePort {

    private final ProductJPARepository repository;
    private final ProductStockRedisService redisService;
    private final ProductStockLogRepository stockLogRepo;

    public ProductPersistenceAdapter(ProductJPARepository repository, ProductStockRedisService redisService,ProductStockLogRepository stockLogRepo) {
        this.repository = repository;
        this.redisService = redisService;
        this.stockLogRepo = stockLogRepo;
    }


    @Override
    public Iterable<Product> findAll() {
        return repository.findAll().stream()
                .map(this::toDomainWithRedisStock)
                .toList();
    }

    @Override
    public Optional<Product> findByProductId(String productId) {
        return repository.findByProductId(productId).map(this::toDomainWithRedisStock);
    }

    private Product toDomainWithRedisStock(ProductEntity entity) {
        String productId = entity.getProductId();
        long stock = 0;

        try {
            stock = redisService.getStock(productId);
        } catch (Exception e) {
            log.warn("[Redis] 캐시에 재고 정보 없음 또는 조회 실패: {}", productId);
            stock = calculateStockFromLog(productId);
            redisService.initStock(productId, stock);
        }

        return new Product.Builder(productId, entity.getProductName())
                .stock(stock)
                .unitPrice(entity.getUnitPrice())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    private long calculateStockFromLog(String productId) {
        List<ProductStockLogEntity> logs = stockLogRepo.findByProductId(productId);

        long increased = logs.stream()
                .filter(log -> log.getType() == ProductStockLogEntity.StockChangeType.INCREASE)
                .mapToLong(ProductStockLogEntity::getQuantity)
                .sum();

        long decreased = logs.stream()
                .filter(log -> log.getType() == ProductStockLogEntity.StockChangeType.DECREASE)
                .mapToLong(ProductStockLogEntity::getQuantity)
                .sum();

        return increased - decreased;
    }

    @Override
    public Product save(Product product) {
        // 1. ProductEntity 저장
        ProductEntity entity = ProductMapper.toEntity(product);
        ProductEntity savedEntity = repository.save(entity);

        // 2. Redis에 초기 재고 저장
        redisService.initStock(savedEntity.getProductId(), product.getStock());

        // 3. Stock Log 저장 (초기화 시 INCREASE 처리)
        ProductStockLogEntity stockLog = ProductStockLogEntity.builder()
                .productId(savedEntity.getProductId())
                .quantity(product.getStock())
                .type(ProductStockLogEntity.StockChangeType.INCREASE)
                .build();

        stockLogRepo.save(stockLog);

        // 4. 도메인 객체로 리턴
        return new Product.Builder(savedEntity.getProductId(), savedEntity.getProductName())
                .stock(product.getStock())
                .unitPrice(savedEntity.getUnitPrice())
                .createdAt(savedEntity.getCreatedAt())
                .build();
    }

    @Override
    public void recordStockIncrease(String productId, long quantity) {
        saveStockLog(productId, quantity, ProductStockLogEntity.StockChangeType.INCREASE);
    }

    @Override
    public void recordStockDecrease(String productId, long quantity) {
        saveStockLog(productId, quantity, ProductStockLogEntity.StockChangeType.DECREASE);
    }

    private void saveStockLog(String productId, long quantity, ProductStockLogEntity.StockChangeType type) {
        log.info("[LogDB] 로그 저장 시도 - 상품: {}, 수량: {}, 타입: {}", productId, quantity, type);
        stockLogRepo.save(ProductStockLogEntity.builder()
                .productId(productId)
                .quantity(quantity)
                .type(type)
                .build());
    }
}
