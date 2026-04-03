package allmart.productservice.application;

import allmart.productservice.adapter.client.InventoryServiceClient;
import allmart.productservice.application.provided.ProductFinder;
import allmart.productservice.application.provided.ProductModifier;
import allmart.productservice.application.provided.ProductRegistrar;
import allmart.productservice.application.required.CategoryRepository;
import allmart.productservice.application.required.ImageStorage;
import allmart.productservice.application.required.ProductRepository;
import allmart.productservice.domain.category.Category;
import allmart.productservice.domain.product.Money;
import allmart.productservice.domain.product.Product;
import allmart.productservice.domain.product.ProductStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService implements ProductRegistrar, ProductFinder, ProductModifier {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ImageStorage imageStorage;
    private final InventoryServiceClient inventoryServiceClient;

    /**
     * 상품 등록 흐름:
     * 1. 이미지 S3 업로드
     * 2. Product 저장 (productId 확정)
     * 3. inventory-service 재고 초기화
     */
    @Override
    @Transactional
    public Product register(Long categoryId, String name, String description, long price, int initialQuantity, String status, MultipartFile image) {
        Category category = findCategoryOrThrow(categoryId);
        String imageUrl = imageStorage.upload(image, "products/temp");

        Product product = Product.create(category, name, description, Money.of(price), imageUrl);
        if ("HIDDEN".equalsIgnoreCase(status)) product.deactivate();
        Product saved = productRepository.save(product);

        initializeInventory(saved.getProductId(), initialQuantity);

        log.info("상품 등록 완료: productId={}, name={}, price={}, categoryId={}, status={}",
                saved.getProductId(), name, price, categoryId, saved.getStatus());
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
    @Transactional
    public Product update(Long productId, String name, String description, Long price, Long categoryId, String status, MultipartFile image) {
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

        product.update(name, description, price != null ? Money.of(price) : null, newImageUrl);

        if ("ON_SALE".equalsIgnoreCase(status)) product.activate();
        else if ("HIDDEN".equalsIgnoreCase(status)) product.deactivate();

        log.info("상품 수정 완료: productId={}, name={}, price={}, status={}", productId, product.getName(), product.getPrice(), product.getStatus());
        return product;
    }

    @Override
    @Transactional
    public void delete(Long productId) {
        Product product = findEditableProduct(productId);
        product.delete();
        log.info("상품 삭제(논리): productId={}, name={}", productId, product.getName());
    }

    private void initializeInventory(Long productId, int quantity) {
        inventoryServiceClient.initialize(productId, quantity);
    }

    private Category findCategoryOrThrow(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카테고리입니다: " + categoryId));
    }

    private Product findEditableProduct(Long productId) {
        return productRepository.findById(productId)
                .filter(p -> p.getStatus() != ProductStatus.DELETED)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다: " + productId));
    }
}
