package allmart.productservice.domain.product;

public enum ProductStatus {
    ACTIVE,    // 판매 중
    INACTIVE,  // 판매 중지 (재활성화 가능)
    DELETED    // 삭제 (터미널, 복구 불가)
}
