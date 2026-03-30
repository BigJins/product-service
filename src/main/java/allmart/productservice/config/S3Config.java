package allmart.productservice.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.net.URI;

/**
 * S3 클라이언트 설정
 *
 * 로컬: MinIO (endpoint=http://localhost:9000, pathStyleAccess=true 필수)
 * 운영: AWS S3 (endpoint 미설정 시 AWS 기본 엔드포인트 사용)
 */
@Configuration
@RequiredArgsConstructor
public class S3Config {

    private final S3Properties s3Properties;

    @Bean
    public S3Client s3Client() {
        var credentialsProvider = StaticCredentialsProvider.create(
                AwsBasicCredentials.create(s3Properties.accessKey(), s3Properties.secretKey())
        );

        var builder = S3Client.builder()
                .credentialsProvider(credentialsProvider)
                .region(Region.of(s3Properties.region()));

        // MinIO 로컬 환경: 커스텀 엔드포인트 + path-style 접근 필수
        if (s3Properties.endpoint() != null && !s3Properties.endpoint().isBlank()) {
            builder
                    .endpointOverride(URI.create(s3Properties.endpoint()))
                    .serviceConfiguration(S3Configuration.builder()
                            .pathStyleAccessEnabled(true)
                            .build());
        }

        return builder.build();
    }
}
