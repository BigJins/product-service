package allmart.productservice.application;

import allmart.productservice.adapter.client.InventoryServiceClient;
import allmart.productservice.application.provided.ProductFinder;
import allmart.productservice.application.provided.ProductModifier;
import allmart.productservice.application.provided.ProductRegistrar;
import allmart.productservice.application.required.CategoryRepository;
import allmart.productservice.application.required.ImageStorage;
import allmart.productservice.application.required.OutboxRepository;
import allmart.productservice.application.required.ProductRepository;
import allmart.productservice.application.required.TaxClassifier;
import allmart.productservice.domain.category.CategoryStatus;
import allmart.productservice.domain.event.OutboxEvent;
import allmart.productservice.domain.product.TaxType;
import allmart.productservice.domain.category.Category;
import allmart.productservice.domain.product.Money;
import allmart.productservice.domain.product.Product;
import allmart.productservice.domain.product.ProductStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService implements ProductRegistrar, ProductFinder, ProductModifier {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ImageStorage imageStorage;
    private final InventoryServiceClient inventoryServiceClient;
    private final TaxClassifier taxClassifier;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    /**
     * 상품 등록 흐름:
     * 1. 이미지 S3 업로드
     * 2. Product 저장 (productId 확정)
     * 3. inventory-service 재고 초기화
     */
    @Override
    @Transactional
    public Product register(Long categoryId, String name, String description, long sellingPrice, long purchasePrice, int initialQuantity, String status, MultipartFile image) {
        Category category = findCategoryOrThrow(categoryId);
        String imageUrl = imageStorage.upload(image, "products/temp");

        Product product = Product.create(category, name, description, Money.of(sellingPrice), Money.of(purchasePrice), imageUrl);
        if ("HIDDEN".equalsIgnoreCase(status)) product.deactivate();
        Product saved = productRepository.save(product);

        // 세금 유형 자동 분류 — 실패 시 PENDING 유지 (상품 등록 자체는 항상 성공)
        // 트레이드오프: 동일 트랜잭션 내 외부 API 호출 → DB 커넥션 최대 5s 추가 점유.
        // 상품 등록은 관리자 저빈도 작업이므로 실용적으로 수용.
        classifyTaxType(saved, name, description);

        initializeInventory(saved.getProductId(), initialQuantity);

        // Outbox: product.registered.v1 → search-service ES 색인
        saveOutbox("product.registered.v1", saved.getProductId(), Map.of(
                "productId", saved.getProductId(),
                "productName", saved.getName(),
                "categoryId", category.getCategoryId(),
                "categoryName", category.getName(),
                "sellingPrice", saved.getSellingPrice(),
                "unit", saved.getUnit() != null ? saved.getUnit() : "",
                "unitSize", saved.getUnitSize() != null ? saved.getUnitSize() : 1,
                "status", saved.getStatus().name(),
                "imageUrl", saved.getImageUrl() != null ? saved.getImageUrl() : "",
                "description", description != null ? description : ""
        ));

        log.info("상품 등록 완료: productId={}, name={}, sellingPrice={}, purchasePrice={}, taxType={}, categoryId={}, status={}",
                saved.getProductId(), name, sellingPrice, purchasePrice, saved.getTaxType(), categoryId, saved.getStatus());
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Product findById(Long productId) {
        return productRepository.findById(productId)
                .filter(p -> p.getStatus() != ProductStatus.DELETED)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다: " + productId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Product> findAll(Long categoryId, Pageable pageable) {
        if (categoryId != null) {
            return productRepository.findByCategory_CategoryIdAndStatus(categoryId, ProductStatus.ACTIVE, pageable);
        }
        return productRepository.findByStatus(ProductStatus.ACTIVE, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Product> findAllForIndex(Pageable pageable) {
        return productRepository.findByStatusNot(ProductStatus.DELETED, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Product> searchByKeyword(String keyword, int size) {
        Pageable pageable = org.springframework.data.domain.PageRequest.of(0, size);
        return productRepository.findByNameContainingIgnoreCaseAndStatus(keyword, ProductStatus.ACTIVE, pageable);
    }

    @Override
    @Transactional
    public Product update(Long productId, String name, String description, Long sellingPrice, Long purchasePrice, Long categoryId, String status, MultipartFile image) {
        Product product = findEditableProduct(productId);

        if (categoryId != null) {
            product.changeCategory(findCategoryOrThrow(categoryId));
        }

        String newImageUrl = null;
        if (image != null && !image.isEmpty()) {
            String newUrl = imageStorage.upload(image, "products/" + productId);
            log.info("상품 이미지 교체: productId={}, oldUrl={}, newUrl={}", productId, product.getImageUrl(), newUrl);
            imageStorage.delete(product.getImageUrl()); // 업로드 성공 후 삭제
            newImageUrl = newUrl;
        }

        product.update(name, description,
                sellingPrice != null ? Money.of(sellingPrice) : null,
                purchasePrice != null ? Money.of(purchasePrice) : null,
                newImageUrl);

        if ("ON_SALE".equalsIgnoreCase(status)) product.activate();
        else if ("HIDDEN".equalsIgnoreCase(status)) product.deactivate();

        log.info("상품 수정 완료: productId={}, name={}, sellingPrice={}, status={}", productId, product.getName(), product.getSellingPrice(), product.getStatus());
        return product;
    }

    @Override
    @Transactional
    public void delete(Long productId) {
        Product product = findEditableProduct(productId);
        product.delete();

        // Outbox: product.deleted.v1 → search-service ES 삭제
        saveOutbox("product.deleted.v1", productId, Map.of(
                "productId", productId,
                "productName", product.getName(),
                "deletedAt", java.time.LocalDateTime.now().toString()
        ));

        log.info("상품 삭제(논리): productId={}, name={}", productId, product.getName());
    }

    private void classifyTaxType(Product product, String name, String description) {
        try {
            TaxType taxType = taxClassifier.classify(name, description);
            product.updateTaxType(taxType);
            log.info("세금 유형 자동 분류 완료: productId={}, taxType={}", product.getProductId(), taxType);
        } catch (Exception e) {
            // LLM 실패 → TAXABLE 유지 (기본값). 배치가 추후 TAX_EXEMPT 여부 재검토
            log.warn("세금 유형 자동 분류 실패, TAXABLE 유지: productId={}, reason={}", product.getProductId(), e.getMessage());
        }
    }

    private void initializeInventory(Long productId, int quantity) {
        inventoryServiceClient.initialize(productId, quantity);
    }

    private void saveOutbox(String eventType, Long productId, Object payload) {
        try {
            String json = objectMapper.writeValueAsString(payload);
            outboxRepository.save(OutboxEvent.create(eventType, String.valueOf(productId), json));
        } catch (JsonProcessingException e) {
            log.error("Outbox 직렬화 실패: eventType={}, productId={}", eventType, productId, e);
        }
    }

    private Category findCategoryOrThrow(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .filter(c -> c.getStatus() != CategoryStatus.DELETED)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카테고리입니다: " + categoryId));
    }

    private Product findEditableProduct(Long productId) {
        return productRepository.findById(productId)
                .filter(p -> p.getStatus() != ProductStatus.DELETED)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다: " + productId));
    }
}
