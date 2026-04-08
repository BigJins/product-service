package allmart.productservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class RestClientConfig {

    @Value("${internal.inventory-service.url:http://localhost:8085}")
    private String inventoryServiceUrl;

    @Value("${anthropic.api-key:}")
    private String anthropicApiKey;

    @Bean
    @RefreshScope
    public RestClient inventoryServiceRestClient() {
        return RestClient.builder()
                .baseUrl(inventoryServiceUrl)
                .requestFactory(requestFactory(3, 5))
                .build();
    }

    @Bean
    @RefreshScope
    public RestClient anthropicRestClient() {
        return RestClient.builder()
                .baseUrl("https://api.anthropic.com")
                .defaultHeader("x-api-key", anthropicApiKey)
                .defaultHeader("anthropic-version", "2023-06-01")
                .defaultHeader("content-type", MediaType.APPLICATION_JSON_VALUE)
                .requestFactory(requestFactory(3, 5))
                .build();
    }

    private SimpleClientHttpRequestFactory requestFactory(int connectSec, int readSec) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(connectSec));
        factory.setReadTimeout(Duration.ofSeconds(readSec));
        return factory;
    }
}
