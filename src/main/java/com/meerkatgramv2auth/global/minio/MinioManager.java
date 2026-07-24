package com.meerkatgramv2auth.global.minio;

import com.meerkatgramv2auth.global.error.custom.FileManagedException;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class MinioManager {
    private final MinioConfig minioConfig;
    private final MinioClient minioClient;


    // 파일 형식 정상적인지 검사 후, 확장자 반환
    public String extractExtension(MultipartFile file) {
        // 파일 존재 체크
        if (file ==null || file.isEmpty()) {
            throw new FileManagedException("파일 업로드 실패: 파일 없음");
        }

        // 파일명 확인 & 파일 확장자 검증
        String fileName = file.getOriginalFilename();
        if (fileName == null || !fileName.contains(".")) {
            throw new FileManagedException("파일 업로드 실패: 파일명 이상");
        }
            // .substring(인덱스번호): 인덱스로 문자열 잘라내기
        String fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();

        // 허용확장자 검증
        if (!minioConfig.allowImageExtensions().contains("image/" + fileExtension)) {
            throw new FileManagedException("파일 업로드 실패: 허용하지 않는 확장자");
        }

        return fileExtension;
    }

    // 랜덤 파일명 생성(yyyyMMdd_파일명)
    public String generateFileName() {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDate now = LocalDate.now();

        return now.format(dateFormatter) + "_" + UUID.randomUUID();
    }

    // MinIO에 파일 저장시 사용하는 객체 키 생성
    // MinIO나 S3는 파일을 Object라고 부르며, 각 파일을 구분하는 '고유한 경로'를 Object Key라고 합니다.
    public String generateObjectKey(MultipartFile file) {

        // path 생성: (저장할 경로(폴더), 파일명.확장자)
        Path path = Path.of(minioConfig.minioProfilePath(), this.generateFileName() + "." + this.extractExtension(file));
        // path의 경로 구분자를 /로 통일(운영체제에 다른 충돌 없도록하기 위함)
        return path.toString().replace(File.separator, "/");
    }

    public void uploadFile(String objectKey, MultipartFile file) {
        try(InputStream inputStream = file.getInputStream()) {
            minioClient
                    .putObject(
                            PutObjectArgs.builder()
                                    .bucket(minioConfig.minioBucket())  // 파일이 저장될 Minio의 버킷명
                                    .object(objectKey)   // 파일 내부에서 관리될 전체 저장 경로
                                    .stream(
                                            inputStream,   // 업로드할 파일의 InputStream
                                            file.getSize(),   // 업로드할 파일의 크기
                                            -1               // 업로드할 패킷 크기(-1은 MinIo가 적절히 조절해서 전송)
                                    )
                                    .contentType(file.getContentType())   // 파일의 Mime 타입
                                    .build()
                    );
        } catch (Exception e) {
            throw new FileManagedException("파일 업로드 실패: MinIO 업로드 실패," + objectKey + e.getMessage());
        }
    }

    public String createMinioObjectUri(String objectKey) {
        Path path = Path.of(minioConfig.minioBucket(), objectKey);

        return String.format(
                "%s/%s",
                minioConfig.minioEndpoint(),
                path.toString().replace(File.separator, "/")
        );
    }
}
