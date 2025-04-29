package org.allmart.productservice.infrastructure.adapter;

import org.allmart.productservice.application.port.out.ProductPersistencePort;
import org.allmart.productservice.domain.Product;
import org.allmart.productservice.infrastructure.entity.ProductEntity;
import org.allmart.productservice.infrastructure.mapper.ProductMapper;
import org.allmart.productservice.infrastructure.redis.ProductStockRedisService;
import org.allmart.productservice.infrastructure.repo.ProductJPARepository;

import java.util.Optional;

public class ProductPersistenceAdapter implements ProductPersistencePort {

    private final ProductJPARepository repository;
    private final ProductStockRedisService redisService;

    public ProductPersistenceAdapter(ProductJPARepository repository, ProductStockRedisService redisService) {
        this.repository = repository;
        this.redisService = redisService;
    }


    @Override
    public Iterable<Product> findAll() {
        return repository.findAll().stream()
                .map(entity -> {
                    Product product = ProductMapper.toDomain(entity);
                    Long stock = redisService.getStock(product.getProductId());
                    return new Product.Builder(product.getProductId(), product.getProductName())
                            .stock(stock.intValue())
                            .unitPrice(product.getUnitPrice())
                            .createdAt(product.getCreatedAt())
                            .build();
                })
                .toList();
    }

    @Override
    public Optional<Product> findByProductId(String productId) {
        return repository.findByProductId(productId).map(entity -> {
            Product product = ProductMapper.toDomain(entity);
            Long stock = redisService.getStock(productId);
            return new Product.Builder(product.getProductId(), product.getProductName())
                    .stock(stock.intValue())
                    .unitPrice(product.getUnitPrice())
                    .createdAt(product.getCreatedAt())
                    .build();
        });
    }

    @Override
    public Product save(Product product) {
        redisService.initStock(product.getProductId(), product.getStock());
        ProductEntity entity = ProductMapper.toEntity(product);
        ProductEntity savedEntity = repository.save(entity);
        return ProductMapper.toDomain(savedEntity);
    }

    @Override
    public void increaseStock(String productId, int quantity) {
        redisService.increaseStock(productId, quantity);
        repository.increaseStock(productId, quantity);
    }

    @Override
    public void decreaseStock(String productId, int quantity) {
        redisService.decreaseStock(productId, quantity);
        repository.decreaseStock(productId, quantity);
    }
}
