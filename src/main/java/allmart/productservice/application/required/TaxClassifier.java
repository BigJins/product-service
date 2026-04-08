package allmart.productservice.application.required;

import allmart.productservice.domain.product.TaxType;

/**
 * 상품명/설명 기반 세금 유형 자동 분류 포트.
 * 구현체: adapter/client/AnthropicTaxClassifier
 */
public interface TaxClassifier {
    /**
     * @return TAXABLE(과세) 또는 TAX_EXEMPT(면세)
     * @throws RuntimeException LLM 호출 실패 시 — 호출 측에서 PENDING 폴백 처리
     */
    TaxType classify(String productName, String description);
}
