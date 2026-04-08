package allmart.productservice.application;

import allmart.productservice.application.provided.CategoryManager;
import allmart.productservice.application.required.CategoryRepository;
import allmart.productservice.domain.category.Category;
import allmart.productservice.domain.category.CategoryStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
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
        return categoryRepository.findAllByStatus(CategoryStatus.ACTIVE);
    }

    @Override
    @Transactional
    public Category rename(Long categoryId, String name) {
        Category category = findActiveOrThrow(categoryId);
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
        Category category = findActiveOrThrow(categoryId);
        category.delete();
        log.info("카테고리 삭제(논리): categoryId={}, name={}", categoryId, category.getName());
    }

    private Category findActiveOrThrow(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .filter(c -> c.getStatus() != CategoryStatus.DELETED)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카테고리입니다: " + categoryId));
    }
}