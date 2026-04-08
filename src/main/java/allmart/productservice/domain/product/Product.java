package allmart.productservice.domain.product;

import allmart.productservice.config.SnowflakeGenerated;
import allmart.productservice.domain.category.Category;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

import static java.util.Objects.requireNonNull;

/**
 * 상품 Aggregate Root
 *
 * 상태 전이:
 *   ACTIVE ↔ INACTIVE (판매 중지/재개)
 *   ACTIVE | INACTIVE → DELETED (소프트 삭제, 터미널)
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product {

    @Id
    @SnowflakeGenerated
    private Long productId;

    private Category category;

    private String name;

    private String description;

    private long sellingPrice;   // 판매가 — 소비자에게 노출되는 가격

    private long purchasePrice;  // 매입가 — 마진 계산 기준

    private TaxType taxType;     // PENDING: LLM 판별 대기, TAXABLE: 과세, TAX_EXEMPT: 면세

    private String imageUrl;

    private ProductStatus status;

    private String unit;        // 판매 단위 예: "박스", "팩", "개"

    private Integer unitSize;   // 단위당 개수 예: 10 (1박스=10개), null이면 1개 단위

    @CreatedDate
    private LocalDateTime createdAt;

    public static Product create(Category category, String name, String description,
                                 Money sellingPrice, Money purchasePrice, String imageUrl) {
        requireNonNull(category, "카테고리는 필수입니다.");
        requireNonNull(name, "상품명은 필수입니다.");
        if (name.isBlank()) throw new IllegalArgumentException("상품명은 비어있을 수 없습니다.");
        requireNonNull(sellingPrice, "판매가는 필수입니다.");
        requireNonNull(purchasePrice, "매입가는 필수입니다.");
        requireNonNull(imageUrl, "이미지 URL은 필수입니다.");

        Product product = new Product();
        product.category = category;
        product.name = name;
        product.description = description;
        product.sellingPrice = sellingPrice.amount();
        product.purchasePrice = purchasePrice.amount();
        product.taxType = TaxType.PENDING; // LLM 분류 전 대기 상태
        product.imageUrl = imageUrl;
        product.status = ProductStatus.ACTIVE;
        return product;
    }

    // --- 파생 값 ---

    public Money getSellingMoney() {
        return Money.of(this.sellingPrice);
    }

    public Money getPurchaseMoney() {
        return Money.of(this.purchasePrice);
    }

    /** 마진 = 판매가 - 매입가 */
    public Money margin() {
        return getSellingMoney().minus(getPurchaseMoney());
    }

    // --- 상태 전이 ---

    public void deactivate() {
        requireNotDeleted();
        this.status = ProductStatus.INACTIVE;
    }

    public void activate() {
        requireNotDeleted();
        this.status = ProductStatus.ACTIVE;
    }

    public void delete() {
        if (this.status == ProductStatus.DELETED) return; // 멱등성
        this.status = ProductStatus.DELETED;
    }

    public void updateTaxType(TaxType taxType) {
        requireNonNull(taxType);
        this.taxType = taxType;
    }

    // --- 정보 수정 ---

    public void changeCategory(Category newCategory) {
        requireNonNull(newCategory, "카테고리는 필수입니다.");
        requireNotDeleted();
        this.category = newCategory;
    }

    public void update(String name, String description, Money sellingPrice, Money purchasePrice, String imageUrl) {
        requireNotDeleted();
        if (name != null && !name.isBlank()) this.name = name;
        if (description != null) this.description = description;
        if (sellingPrice != null) this.sellingPrice = sellingPrice.amount();
        if (purchasePrice != null) this.purchasePrice = purchasePrice.amount();
        if (imageUrl != null && !imageUrl.isBlank()) this.imageUrl = imageUrl;
    }

    public void updateUnit(String unit, Integer unitSize) {
        requireNotDeleted();
        this.unit = unit;
        this.unitSize = unitSize;
    }

    public boolean isVisible() {
        return this.status == ProductStatus.ACTIVE;
    }

    private void requireNotDeleted() {
        if (this.status == ProductStatus.DELETED) {
            throw new IllegalStateException("삭제된 상품은 수정할 수 없습니다.");
        }
    }
}
