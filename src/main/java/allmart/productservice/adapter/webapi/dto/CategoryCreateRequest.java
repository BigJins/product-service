package allmart.productservice.adapter.webapi.dto;

import jakarta.validation.constraints.NotBlank;

public record CategoryCreateRequest(
        @NotBlank(message = "카테고리명은 필수입니다.")
        String name
) {}
