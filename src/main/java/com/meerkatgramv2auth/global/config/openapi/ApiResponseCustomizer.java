package com.meerkatgramv2auth.global.config.openapi;

import com.meerkatgramv2auth.global.response.constant.CustomResponseCode;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.responses.ApiResponse;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;

import java.util.*;

// implements OperationCustomizer: OperationCustomizer라는 인터페이스를 상속하여 오버라이드 할 것임
@Component
public class ApiResponseCustomizer implements OperationCustomizer {

    // operation: Swagger가 만드는 문서(경로, 응답, 파라미터 등)
    // handlerMethod: 클라이언트의 req에 맞는 "컨트롤러의 메서드"(spring MVC가 매칭해줌)
    @Override
    public Operation customize(Operation operation, HandlerMethod handlerMethod) {

        // .getMethodAnnotation: 메서드에 @CustomApiResponse어노테이션이 붙어있는지 확인
        // 어노테이션 없으면, 원래 operation 반환
        CustomApiResponse annotation = handlerMethod.getMethodAnnotation(CustomApiResponse.class);
        if (annotation == null) {
            return operation;
        }

        // HTTP 상태 코드별 에러 모음집
            // Map<key, value>: Key와 Value를 쌍으로 저장하는 보관함
            // key-Integer: HTTP 상태 코드(400, 404, 500...)
            // Value-List<CustomResponseCode>: 에러코드 들
            // = new HashMap<>();: 이 구조를 가진 실제 빈 보관함을 메모리에 새로 생성
        Map<Integer, List<CustomResponseCode>> errorCodeMap = new HashMap<>();

        // annotation.value()(컨트롤러의 메소드에 등록된 에러 목록) 불러와서 injectErrorCode에 담기
        // injectErrorCode.getHttpStatus().value(): 특정 에러 코드 당, 설정된 HTTP 상태 코드(404...)
        // 위에 상태코드. errorCodeMap에 담기
        for(CustomResponseCode injectErrorCode : annotation.value()) {
            int httpStatus = injectErrorCode.getHttpStatus().value();
            errorCodeMap.computeIfAbsent(httpStatus, item -> new ArrayList<>()).add(injectErrorCode);
        }


        errorCodeMap.forEach((httpStatus, customErrorCodeList) -> {
            Content content = new Content();
            MediaType mediaType = new MediaType();

            customErrorCodeList.forEach(customErrorCode -> {
                Map<String, Object> exampleMap = new LinkedHashMap<>();
                exampleMap.put("code", customErrorCode.getCode());
                exampleMap.put("message", customErrorCode.name());
                exampleMap.put("data", null);
                mediaType.addExamples(customErrorCode.name(), new Example().value(exampleMap));
            });
            content.addMediaType("application/json", mediaType);

            operation.getResponses().addApiResponse(
                    String.valueOf(httpStatus),
                    new ApiResponse().description("에러 응답").content(content)
            );
        });

        return operation;
    }
}