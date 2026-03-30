package allmart.productservice.domain.category;

import allmart.productservice.domain.AbstractEntity;
import jakarta.persistence.*;
import lombok.Getter;

import java.util.Objects;

@Entity
@Table(name = "tbl_category")
@Getter
public class Category extends AbstractEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long categoryId;

    @Column(nullable = false, unique = true)
    private String name;

    protected Category() {}

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
