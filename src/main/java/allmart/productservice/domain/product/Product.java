package allmart.productservice.domain.product;

import allmart.productservice.config.SnowflakeGenerated;
import allmart.productservice.domain.AbstractEntity;
import allmart.productservice.domain.category.Category;
import jakarta.persistence.*;
import lombok.Getter;

import java.util.Objects;

/**
 * 상품 Aggregate Root
 *
 * 상태 전이:
 *   ACTIVE ↔ INACTIVE (판매 중지/재개)
 *   ACTIVE | INACTIVE → DELETED (소프트 삭제, 터미널)
 */
@Entity
@Table(name = "tbl_product")
@Getter
public class Product extends AbstractEntity {

    @Id
    @SnowflakeGenerated
    private Long productId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private long price;  // Money.amount() — DB 저장은 primitive

    @Column(nullable = false)
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductStatus status;

    protected Product() {}

    private Product(Category category, String name, String description, Money price, String imageUrl) {
        this.category = category;
        this.name = name;
        this.description = description;
        this.price = price.amount();
        this.imageUrl = imageUrl;
        this.status = ProductStatus.ACTIVE;
    }

    public static Product create(Category category, String name, String description,
                                  Money price, String imageUrl) {
        Objects.requireNonNull(category, "카테고리는 필수입니다.");
        Objects.requireNonNull(name, "상품명은 필수입니다.");
        if (name.isBlank()) throw new IllegalArgumentException("상품명은 비어있을 수 없습니다.");
        Objects.requireNonNull(price, "가격은 필수입니다.");
        Objects.requireNonNull(imageUrl, "이미지 URL은 필수입니다.");
        return new Product(category, name, description, price, imageUrl);
    }

    // --- 상태 전이 ---

    public void deactivate() {
        if (this.status == ProductStatus.DELETED) {
            throw new IllegalStateException("삭제된 상품은 상태를 변경할 수 없습니다.");
        }
        this.status = ProductStatus.INACTIVE;
    }

    public void activate() {
        if (this.status == ProductStatus.DELETED) {
            throw new IllegalStateException("삭제된 상품은 상태를 변경할 수 없습니다.");
        }
        this.status = ProductStatus.ACTIVE;
    }

    public void delete() {
        if (this.status == ProductStatus.DELETED) return; // 멱등성
        this.status = ProductStatus.DELETED;
    }

    // --- 정보 수정 ---

    public void changeCategory(Category newCategory) {
        Objects.requireNonNull(newCategory, "카테고리는 필수입니다.");
        if (this.status == ProductStatus.DELETED) throw new IllegalStateException("삭제된 상품은 수정할 수 없습니다.");
        this.category = newCategory;
    }

    public void update(String name, String description, Money price, String imageUrl) {
        if (this.status == ProductStatus.DELETED) {
            throw new IllegalStateException("삭제된 상품은 수정할 수 없습니다.");
        }
        if (name != null && !name.isBlank()) this.name = name;
        if (description != null) this.description = description;
        if (price != null) this.price = price.amount();
        if (imageUrl != null && !imageUrl.isBlank()) this.imageUrl = imageUrl;
    }

    public Money getMoneyPrice() {
        return Money.of(this.price);
    }

    public boolean isVisible() {
        return this.status == ProductStatus.ACTIVE;
    }
}
