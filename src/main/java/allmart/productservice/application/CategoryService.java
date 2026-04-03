package allmart.productservice.application;

import allmart.productservice.application.provided.CategoryManager;
import allmart.productservice.application.required.CategoryRepository;
import allmart.productservice.domain.category.Category;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Log4j2
@Service
@RequiredArgsConstructor
public class CategoryService implements CategoryManager {

    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public Category create(String name) {
        if (categoryRepository.existsByName(name)) {
            log.warn("카테고리 생성 실패 - 이미 존재: {}", name);
            throw new IllegalStateException("이미 존재하는 카테고리입니다: " + name);
        }
        Category saved = categoryRepository.save(Category.create(name));
        log.info("카테고리 생성 완료: categoryId={}, name={}", saved.getCategoryId(), name);
        return saved;
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
            log.warn("카테고리 이름 변경 실패 - 이미 존재하는 이름: {}", name);
            throw new IllegalStateException("이미 존재하는 카테고리명입니다: " + name);
        }
        String oldName = category.getName();
        category.rename(name);
        log.info("카테고리 이름 변경: categoryId={}, {} → {}", categoryId, oldName, name);
        return category;
    }

    @Override
    @Transactional
    public void delete(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카테고리입니다: " + categoryId));
        categoryRepository.deleteById(categoryId);
        log.info("카테고리 삭제: categoryId={}, name={}", categoryId, category.getName());
    }
}
