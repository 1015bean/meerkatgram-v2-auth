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

    // 파일 업로드
        // objectKey: 오브젝트의 key(파일을 저장할 주소)
    public void uploadFile(String objectKey, MultipartFile file) {

        // InputStream: 파일 데이터를 바이트 형식으로 읽어내는 통로(파일을 실재로 전송하기 위해 형태를 변환)
        try(InputStream inputStream = file.getInputStream()) {
            // MinIO 서버에 요청 보내기
                // .putObject: 파일 저장 req   PutObjectArgs.builder(): 옵션 설정
            minioClient
                    .putObject(
                            PutObjectArgs.builder()
                                    .bucket(minioConfig.minioBucket())  // 파일이 저장될 Minio의 버킷명
                                    .object(objectKey)   // 파일명 & 저장될 파일 경로
                                    .stream(
                                            inputStream,   // 업로드할 파일의 InputStream
                                            file.getSize(),   // 업로드할 파일의 크기
                                            -1               // 업로드할 패킷 크기(-1은 MinIo가 적절히 조절해서 전송)
                                    )
                                    .contentType(file.getContentType())   // 파일의 Mime 타입(사진, 텍스트, PDF..)
                                    .build()
                    );
        } catch (Exception e) {
            throw new FileManagedException("파일 업로드 실패: MinIO 업로드 실패," + objectKey + e.getMessage());
        }
    }

    // 업로드된 파일의 URL를 생성
    public String createMinioObjectUri(String objectKey) {

        // Path.of(): 경로를 운영체제에 맞게 합쳐줌
        Path path = Path.of(minioConfig.minioBucket(), objectKey);

        // String.format( 형태"요소1/요소2", 요소1, 요소2 )
        return String.format(
                "%s/%s",
                minioConfig.minioEndpoint(),
                path.toString().replace(File.separator, "/")   // 운영체제에 맞는 경로 문자열을 반환
        );
    }
}
