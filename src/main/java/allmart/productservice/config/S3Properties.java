package allmart.productservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "aws.s3")
public record S3Properties(
        String endpoint,    // MinIO: http://localhost:9000 / AWS S3: null (기본 엔드포인트 사용)
        String bucket,
        String accessKey,
        String secretKey,
        String region
) {}
