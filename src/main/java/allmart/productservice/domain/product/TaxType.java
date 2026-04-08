package allmart.productservice.domain.product;

/**
 * 상품 세금 유형
 *
 * PENDING    — LLM 분류 대기 중 (등록 직후 기본값)
 * TAXABLE    — 부가세 과세 (10%)
 * TAX_EXEMPT — 부가세 면세 (기초생활필수품: 가공하지 않은 농수축산물, 의약품, 의료용역 등)
 *
 * LLM 분류 실패 시 TAXABLE로 폴백 — 불확실할 때 세금을 더 내는 것이 세무 리스크가 없음.
 */
public enum TaxType {
    PENDING,    // LLM 분류 대기
    TAXABLE,
    TAX_EXEMPT
}