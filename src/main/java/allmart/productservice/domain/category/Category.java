package allmart.productservice.domain.category;

import allmart.productservice.config.SnowflakeGenerated;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "tbl_category")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category {

    @Id
    @SnowflakeGenerated
    private Long categoryId;

    @Column(nullable = false, unique = true)
    private String name;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    private Category(String name) {
        this.name = name;
    }

    public static Category create(String name) {
        Objects.requireNonNull(name, "카테고리명은 필수입니다.");
        if (name.isBlank()) throw new IllegalArgumentException("카테고리명은 비어있을 수 없습니다.");
        return new Category(name);
    }

    public void rename(String newName) {
        Objects.requireNonNull(newName, "카테고리명은 필수입니다.");
        if (newName.isBlank()) throw new IllegalArgumentException("카테고리명은 비어있을 수 없습니다.");
        this.name = newName;
    }
}
