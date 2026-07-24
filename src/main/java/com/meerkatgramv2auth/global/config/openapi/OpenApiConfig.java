package com.meerkatgramv2auth.global.config.openapi;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    private static final String BEARER_AUTH = "bearerAuth";

    // OpenAPI에 인증 방식을 등록하는 설정
    @Bean
    public OpenAPI customOpenApi() {
        return new OpenAPI()
                .info(
                  new Info()
                          .title("Meerkatgram Auth API")  // 문서의 제목
                          .description("Meerkatgram Auth REAT API Document") // 문서 설명
                          .version("v1.0.0") // 문서의 버전
                )
                .components(new Components().addSecuritySchemes(BEARER_AUTH,  // OpenAPI 문서에서 공통으로 사용할 설정을 등록, new Components().addSecuritySchemes(): 새로운 인증 방식 하나를 swagger에 등록
                        new SecurityScheme()   // 인증방식 세부 설정
                                .type(SecurityScheme.Type.HTTP)  // -> HTTP 인증
                                .scheme("bearer")  // -> Bearer Token 방식
                                .bearerFormat("JWT")))  // -> Bearer Token은 JWT
                .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH));  // .addSecurityItem(): 등록한 인증 방식을 사용할 대상(API)에 적용
    }
}
