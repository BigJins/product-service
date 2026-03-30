package allmart.productservice.adapter.webapi.dto;

import allmart.productservice.domain.category.Category;

public record CategoryResponse(Long categoryId, String name) {
    public static CategoryResponse of(Category c) {
        return new CategoryResponse(c.getCategoryId(), c.getName());
    }
}
