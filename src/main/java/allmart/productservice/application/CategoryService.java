package allmart.productservice.application;

import allmart.productservice.application.provided.CategoryManager;
import allmart.productservice.application.required.CategoryRepository;
import allmart.productservice.domain.category.Category;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService implements CategoryManager {

    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public Category create(String name) {
        if (categoryRepository.existsByName(name)) {
            throw new IllegalStateException("이미 존재하는 카테고리입니다: " + name);
        }
        return categoryRepository.save(Category.create(name));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    @Override
    @Transactional
    public Category rename(Long categoryId, String name) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카테고리입니다: " + categoryId));
        if (categoryRepository.existsByName(name)) {
            throw new IllegalStateException("이미 존재하는 카테고리명입니다: " + name);
        }
        category.rename(name);
        return category;
    }

    @Override
    @Transactional
    public void delete(Long categoryId) {
        categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카테고리입니다: " + categoryId));
        categoryRepository.deleteById(categoryId);
    }
}
