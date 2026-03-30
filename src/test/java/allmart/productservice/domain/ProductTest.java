package allmart.productservice.domain;

import allmart.productservice.domain.category.Category;
import allmart.productservice.domain.product.Money;
import allmart.productservice.domain.product.Product;
import allmart.productservice.domain.product.ProductStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class ProductTest {

    private Category category;

    @BeforeEach
    void setUp() {
        category = Category.create("과일");
    }

    @Test
    @DisplayName("상품 생성 시 기본 상태는 ACTIVE다")
    void create_default_status_is_active() {
        Product product = Product.create(category, "제주 감귤", "달콤한 감귤", Money.of(15000), "http://img.url");
        assertThat(product.getStatus()).isEqualTo(ProductStatus.ACTIVE);
    }

    @Test
    @DisplayName("상품을 비활성화하면 INACTIVE 상태가 된다")
    void deactivate_changes_status_to_inactive() {
        Product product = Product.create(category, "감귤", null, Money.of(10000), "http://img.url");
        product.deactivate();
        assertThat(product.getStatus()).isEqualTo(ProductStatus.INACTIVE);
    }

    @Test
    @DisplayName("비활성화된 상품을 다시 활성화하면 ACTIVE 상태가 된다")
    void activate_inactive_product() {
        Product product = Product.create(category, "감귤", null, Money.of(10000), "http://img.url");
        product.deactivate();
        product.activate();
        assertThat(product.getStatus()).isEqualTo(ProductStatus.ACTIVE);
    }

    @Test
    @DisplayName("삭제된 상품은 멱등성이 보장된다")
    void delete_is_idempotent() {
        Product product = Product.create(category, "감귤", null, Money.of(10000), "http://img.url");
        product.delete();
        product.delete(); // 두 번 호출해도 예외 없음
        assertThat(product.getStatus()).isEqualTo(ProductStatus.DELETED);
    }

    @Test
    @DisplayName("삭제된 상품은 상태를 변경할 수 없다")
    void deleted_product_cannot_change_status() {
        Product product = Product.create(category, "감귤", null, Money.of(10000), "http://img.url");
        product.delete();
        assertThatThrownBy(product::activate).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(product::deactivate).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("상품 정보를 수정하면 변경된다")
    void update_changes_product_info() {
        Product product = Product.create(category, "감귤", null, Money.of(10000), "http://old.url");
        product.update("한라봉", "달달", Money.of(20000), "http://new.url");
        assertThat(product.getName()).isEqualTo("한라봉");
        assertThat(product.getPrice()).isEqualTo(20000);
        assertThat(product.getImageUrl()).isEqualTo("http://new.url");
    }

    @Test
    @DisplayName("삭제된 상품은 수정할 수 없다")
    void deleted_product_cannot_be_updated() {
        Product product = Product.create(category, "감귤", null, Money.of(10000), "http://img.url");
        product.delete();
        assertThatThrownBy(() -> product.update("한라봉", null, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("가격이 음수면 Money 생성 시 예외가 발생한다")
    void negative_price_throws() {
        assertThatThrownBy(() -> Money.of(-1)).isInstanceOf(IllegalArgumentException.class);
    }
}
