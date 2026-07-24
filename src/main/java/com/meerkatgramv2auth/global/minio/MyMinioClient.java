package com.meerkatgramv2auth.global.minio;

import io.minio.MinioClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MyMinioClient {

    // MinioClient 생성
    // : Java 프로그램이 MinIO 서버와 통신하기 위한 객체
    @Bean
    public MinioClient minioClient(MinioConfig minioConfig) {
        return MinioClient.builder()
                .endpoint(minioConfig.minioEndpoint())   // 어느 MinIO 서버에 접속할지 지정
                .credentials(minioConfig.minioAccessKey(), minioConfig.minioSecretKey())  //MinIO 로그인 정보(아이디, 비번)
                .build();
    }
}
