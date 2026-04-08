package allmart.productservice.application.required;

import allmart.productservice.domain.category.Category;
import allmart.productservice.domain.category.CategoryStatus;
import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends Repository<Category, Long> {
    Category save(Category category);
    Optional<Category> findById(Long categoryId);
    boolean existsByName(String name);
    List<Category> findAllByStatus(CategoryStatus status);
}