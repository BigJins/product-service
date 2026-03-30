package allmart.productservice.adapter.webapi;

import allmart.productservice.adapter.webapi.dto.ProductResponse;
import allmart.productservice.adapter.webapi.dto.ProductSummaryResponse;
import allmart.productservice.application.provided.ProductFinder;
import allmart.productservice.application.provided.ProductModifier;
import allmart.productservice.application.provided.ProductRegistrar;
import allmart.productservice.domain.product.Product;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Validated
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductApi {

    private final ProductRegistrar productRegistrar;
    private final ProductFinder productFinder;
    private final ProductModifier productModifier;

    /**
     * 상품 등록 (MEMBER 전용 — Gateway에서 역할 검증 완료)
     * multipart/form-data: image 파일 + 상품 정보 파라미터
     */
    @PostMapping(consumes = "multipart/form-data")
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse register(
            @RequestParam @Positive(message = "카테고리 ID는 양수여야 합니다.") Long categoryId,
            @RequestParam @NotBlank(message = "상품명은 필수입니다.") String name,
            @RequestParam(required = false) String description,
            @RequestParam @Positive(message = "가격은 양수여야 합니다.") long price,
            @RequestParam(name = "stock", defaultValue = "0") int initialQuantity,
            @RequestParam(required = false, defaultValue = "ON_SALE") String status,
            @RequestPart("image") MultipartFile image) {

        Product product = productRegistrar.register(categoryId, name, description, price, initialQuantity, status, image);
        return ProductResponse.of(product);
    }

    /** 상품 단건 조회 */
    @GetMapping("/{productId}")
    public ProductResponse find(@PathVariable Long productId) {
        return ProductResponse.of(productFinder.findById(productId));
    }

    /** 상품 목록 조회 (카테고리 필터 + 페이징) */
    @GetMapping
    public Page<ProductSummaryResponse> findAll(
            @RequestParam(required = false) Long categoryId,
            @PageableDefault(size = 20, sort = "productId", direction = Sort.Direction.DESC) Pageable pageable) {

        return productFinder.findAll(categoryId, pageable).map(ProductSummaryResponse::of);
    }

    /** 상품 수정 (MEMBER 전용) */
    @PutMapping(value = "/{productId}", consumes = "multipart/form-data")
    public ProductResponse update(
            @PathVariable Long productId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) Long price,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String status,
            @RequestPart(value = "image", required = false) MultipartFile image) {

        return ProductResponse.of(productModifier.update(productId, name, description, price, categoryId, status, image));
    }

    /** 상품 삭제 — soft delete (MEMBER 전용) */
    @DeleteMapping("/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long productId) {
        productModifier.delete(productId);
    }
}
