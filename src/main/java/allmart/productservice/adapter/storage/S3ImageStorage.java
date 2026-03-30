package allmart.productservice.adapter.storage;

import allmart.productservice.application.required.ImageStorage;
import allmart.productservice.config.S3Properties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;

/**
 * S3 이미지 업로드 구현체
 *
 * MinIO(로컬) / AWS S3(운영) 동일 코드 — endpoint만 환경변수로 분기
 * 업로드 전 Thumbnailator로 800x800 리사이즈 → 용량/전송비 절감
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class S3ImageStorage implements ImageStorage {

    private static final int MAX_DIMENSION = 800;
    private static final float QUALITY = 0.85f;
    private static final String CONTENT_TYPE = "image/jpeg";

    private final S3Client s3Client;
    private final S3Properties s3Properties;

    @Override
    public String upload(MultipartFile file, String directory) {
        String key = directory + "/" + UUID.randomUUID() + ".jpg";

        byte[] resized = resize(file);

        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(s3Properties.bucket())
                        .key(key)
                        .contentType(CONTENT_TYPE)
                        .contentLength((long) resized.length)
                        .acl(ObjectCannedACL.PUBLIC_READ)
                        .build(),
                RequestBody.fromBytes(resized)
        );

        String url = buildUrl(key);
        log.info("이미지 업로드 완료: {}", url);
        return url;
    }

    @Override
    public void delete(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) return;
        try {
            String key = extractKey(imageUrl);
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(s3Properties.bucket())
                    .key(key)
                    .build());
            log.info("이미지 삭제 완료: {}", key);
        } catch (Exception e) {
            log.warn("이미지 삭제 실패 (계속 진행): url={}, error={}", imageUrl, e.getMessage());
        }
    }

    private byte[] resize(MultipartFile file) {
        try (var out = new ByteArrayOutputStream()) {
            Thumbnails.of(file.getInputStream())
                    .size(MAX_DIMENSION, MAX_DIMENSION)
                    .outputFormat("jpg")
                    .outputQuality(QUALITY)
                    .toOutputStream(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new IllegalArgumentException("이미지 처리 실패: " + e.getMessage(), e);
        }
    }

    private String buildUrl(String key) {
        String endpoint = s3Properties.endpoint();
        if (endpoint != null && !endpoint.isBlank()) {
            // MinIO: http://localhost:9000/bucket/key
            return endpoint + "/" + s3Properties.bucket() + "/" + key;
        }
        // AWS S3: https://bucket.s3.region.amazonaws.com/key
        return "https://" + s3Properties.bucket()
                + ".s3." + s3Properties.region()
                + ".amazonaws.com/" + key;
    }

    private String extractKey(String imageUrl) {
        // URL에서 bucket 이후 경로를 key로 추출
        String bucket = s3Properties.bucket();
        int idx = imageUrl.indexOf(bucket);
        if (idx == -1) return imageUrl;
        return imageUrl.substring(idx + bucket.length() + 1);
    }
}
