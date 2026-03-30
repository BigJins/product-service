package allmart.productservice.application.provided;

import allmart.productservice.domain.category.Category;

import java.util.List;

public interface CategoryManager {
    Category create(String name);
    List<Category> findAll();
    Category rename(Long categoryId, String name);
    void delete(Long categoryId);
}
