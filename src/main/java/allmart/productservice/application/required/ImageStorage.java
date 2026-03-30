package allmart.productservice.application.required;

import org.springframework.web.multipart.MultipartFile;

public interface ImageStorage {

    /**
     * 이미지를 업로드하고 접근 가능한 URL을 반환합니다.
     *
     * @param file      업로드할 이미지 파일
     * @param directory S3 내 디렉토리 경로 (예: "products/42")
     * @return 업로드된 이미지의 공개 URL
     */
    String upload(MultipartFile file, String directory);

    /**
     * URL로부터 객체를 삭제합니다.
     */
    void delete(String imageUrl);
}
