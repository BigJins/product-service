package org.allmart.productservice.infrastructure.repo;

import org.allmart.productservice.infrastructure.entity.ProductStockLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductStockLogRepository extends JpaRepository<ProductStockLogEntity, Long> {
    List<ProductStockLogEntity> findByProductId(String productId);
}