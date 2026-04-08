package allmart.productservice.adapter.client.dto;

import java.util.List;

public record AnthropicResponse(List<Content> content) {
    public record Content(String type, String text) {}

    /** 첫 번째 text block의 내용 반환 */
    public String firstText() {
        return content.stream()
                .filter(c -> "text".equals(c.type()))
                .map(Content::text)
                .findFirst()
                .orElse("");
    }
}