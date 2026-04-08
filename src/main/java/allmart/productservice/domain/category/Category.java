package allmart.productservice.domain.category;

import allmart.productservice.config.SnowflakeGenerated;
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
 * 카테고리 Aggregate Root
 *
 * 상태 전이:
 *   ACTIVE → DELETED (소프트 딜리트, 터미널)
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category {

    @Id
    @SnowflakeGenerated
    private Long categoryId;

    private String name;

    private CategoryStatus status;

    @CreatedDate
    private LocalDateTime createdAt;

    public static Category create(String name) {
        requireNonNull(name, "카테고리명은 필수입니다.");
        if (name.isBlank()) throw new IllegalArgumentException("카테고리명은 비어있을 수 없습니다.");

        Category category = new Category();
        category.name = name;
        category.status = CategoryStatus.ACTIVE;
        return category;
    }

    public void rename(String newName) {
        requireNotDeleted();
        requireNonNull(newName, "카테고리명은 필수입니다.");
        if (newName.isBlank()) throw new IllegalArgumentException("카테고리명은 비어있을 수 없습니다.");
        this.name = newName;
    }

    public void delete() {
        if (this.status == CategoryStatus.DELETED) return; // 멱등성
        this.status = CategoryStatus.DELETED;
    }

    private void requireNotDeleted() {
        if (this.status == CategoryStatus.DELETED) {
            throw new IllegalStateException("삭제된 카테고리는 수정할 수 없습니다.");
        }
    }
}