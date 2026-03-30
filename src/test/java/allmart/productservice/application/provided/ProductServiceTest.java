package allmart.productservice.application.provided;

import allmart.productservice.application.required.ImageStorage;
import allmart.productservice.domain.product.Product;
import allmart.productservice.domain.product.ProductStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ProductServiceTest {

    // S3 호출 없이 테스트하기 위한 가짜 구현체
    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        public ImageStorage fakeImageStorage() {
            return new ImageStorage() {
                @Override
                public String upload(MultipartFile file, String directory) {
                    return "http://fake-s3/" + directory + "/" + UUID.randomUUID() + ".jpg";
                }
                @Override
                public void delete(String imageUrl) { /* no-op */ }
            };
        }
    }

    CategoryManager categoryManager;
    ProductRegistrar productRegistrar;
    ProductFinder productFinder;
    ProductModifier productModifier;

    ProductServiceTest(CategoryManager categoryManager, ProductRegistrar productRegistrar,
                       ProductFinder productFinder, ProductModifier productModifier) {
        this.categoryManager = categoryManager;
        this.productRegistrar = productRegistrar;
        this.productFinder = productFinder;
        this.productModifier = productModifier;
    }

    private MockMultipartFile dummyImage() {
        return new MockMultipartFile("image", "test.jpg", "image/jpeg", "fake-image".getBytes());
    }

    @Test
    @DisplayName("카테고리 생성 후 상품을 등록하면 ACTIVE 상태로 저장된다")
    void register_product_is_active() {
        var category = categoryManager.create("과일");
        Product product = productRegistrar.register(category.getCategoryId(), "제주 감귤", "달콤", 15000, 100, "ON_SALE", dummyImage());
        assertThat(product.getProductId()).isNotNull();
        assertThat(product.getStatus()).isEqualTo(ProductStatus.ACTIVE);
    }

    @Test
    @DisplayName("중복 카테고리명 등록 시 예외가 발생한다")
    void duplicate_category_throws() {
        categoryManager.create("채소");
        assertThatThrownBy(() -> categoryManager.create("채소"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("이미 존재하는 카테고리");
    }

    @Test
    @DisplayName("상품 삭제 후 조회하면 예외가 발생한다")
    void deleted_product_not_found() {
        var category = categoryManager.create("유제품");
        Product product = productRegistrar.register(category.getCategoryId(), "우유", null, 2000, 50, "ON_SALE", dummyImage());
        productModifier.delete(product.getProductId());

        assertThatThrownBy(() -> productFinder.findById(product.getProductId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("존재하지 않는 상품");
    }

    @Test
    @DisplayName("존재하지 않는 카테고리로 상품 등록 시 예외가 발생한다")
    void register_with_unknown_category_throws() {
        assertThatThrownBy(() -> productRegistrar.register(999L, "감귤", null, 10000, 0, "ON_SALE", dummyImage()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("존재하지 않는 카테고리");
    }

    @Test
    @DisplayName("상품 수정 시 이름과 가격이 변경된다")
    void update_changes_name_and_price() {
        var category = categoryManager.create("음료");
        Product product = productRegistrar.register(category.getCategoryId(), "콜라", null, 1500, 30, "ON_SALE", dummyImage());
        productModifier.update(product.getProductId(), "제로콜라", null, 1800L, null, null, null);

        Product updated = productFinder.findById(product.getProductId());
        assertThat(updated.getName()).isEqualTo("제로콜라");
        assertThat(updated.getPrice()).isEqualTo(1800);
    }
}
