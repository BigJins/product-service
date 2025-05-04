package org.allmart.productservice.infrastructure.repo;

import org.allmart.productservice.infrastructure.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductJPARepository extends JpaRepository<ProductEntity, Long> {
    Optional<ProductEntity> findByProductId(String productId);
}
