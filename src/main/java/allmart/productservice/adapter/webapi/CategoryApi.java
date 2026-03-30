package allmart.productservice.adapter.webapi;

import allmart.productservice.adapter.webapi.dto.CategoryCreateRequest;
import allmart.productservice.adapter.webapi.dto.CategoryResponse;
import allmart.productservice.application.provided.CategoryManager;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryApi {

    private final CategoryManager categoryManager;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponse create(@Valid @RequestBody CategoryCreateRequest request) {
        return CategoryResponse.of(categoryManager.create(request.name()));
    }

    @GetMapping
    public List<CategoryResponse> findAll() {
        return categoryManager.findAll().stream().map(CategoryResponse::of).toList();
    }

    @PutMapping("/{categoryId}")
    public CategoryResponse rename(
            @PathVariable Long categoryId,
            @Valid @RequestBody CategoryCreateRequest request) {
        return CategoryResponse.of(categoryManager.rename(categoryId, request.name()));
    }

    @DeleteMapping("/{categoryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long categoryId) {
        categoryManager.delete(categoryId);
    }
}
