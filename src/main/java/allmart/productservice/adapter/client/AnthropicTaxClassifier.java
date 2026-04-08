package allmart.productservice.adapter.client;

import allmart.productservice.adapter.client.dto.AnthropicRequest;
import allmart.productservice.adapter.client.dto.AnthropicResponse;
import allmart.productservice.application.required.TaxClassifier;
import allmart.productservice.domain.product.TaxType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

/**
 * Anthropic Claude API를 이용한 세금 유형 자동 분류기.
 *
 * <p>한국 부가가치세법 기준으로 상품명/설명을 분석하여 TAXABLE/TAX_EXEMPT를 판별.
 * 면세 대상: 가공하지 않은 농수축산물, 의약품, 교육 서비스 등.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AnthropicTaxClassifier implements TaxClassifier {

    private static final String MODEL = "claude-haiku-4-5-20251001";
    private static final int MAX_TOKENS = 10;

    private static final String PROMPT_TEMPLATE = """
            한국 부가가치세법 기준으로 아래 상품이 과세 대상인지 면세 대상인지 판단하세요.
            과세이면 TAXABLE, 면세이면 TAX_EXEMPT 라고 단어 하나만 답하세요.

            상품명: %s
            설명: %s
            """;

    private final RestClient anthropicRestClient;

    @Override
    public TaxType classify(String productName, String description) {
        String prompt = PROMPT_TEMPLATE.formatted(
                productName,
                description != null ? description : "없음"
        );

        AnthropicRequest body = new AnthropicRequest(
                MODEL,
                MAX_TOKENS,
                List.of(new AnthropicRequest.Message("user", prompt))
        );

        AnthropicResponse response = anthropicRestClient.post()
                .uri("/v1/messages")
                .body(body)
                .retrieve()
                .body(AnthropicResponse.class);

        String text = response != null ? response.firstText().trim().toUpperCase() : "";
        log.debug("Anthropic 세금 분류 응답: productName={}, raw={}", productName, text);

        if (text.contains("TAX_EXEMPT")) return TaxType.TAX_EXEMPT;
        if (text.contains("TAXABLE"))   return TaxType.TAXABLE;

        throw new IllegalStateException("Anthropic 응답 파싱 실패: " + text);
    }
}