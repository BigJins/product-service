package org.allmart.productservice.infrastructure.repo;

import org.allmart.productservice.infrastructure.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;


public interface ProductJPARepository extends JpaRepository<ProductEntity, Long> {
    Optional<ProductEntity> findByProductId(String productId);

    @Transactional
    @Modifying
    @Query("UPDATE ProductEntity p SET p.stock = p.stock + :quantity WHERE p.productId = :productId")
    void increaseStock(String testId, int quantity);

    @Transactional
    @Modifying
    @Query("UPDATE ProductEntity p SET p.stock = p.stock - :quantity WHERE p.productId = :productId AND p.stock >= :quantity")
    void decreaseStock(String testId, int quantity);
}
