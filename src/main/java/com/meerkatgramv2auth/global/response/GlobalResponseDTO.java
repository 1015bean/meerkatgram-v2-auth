package com.meerkatgramv2auth.global.response;

import com.meerkatgramv2auth.global.response.constant.CustomResponseCode;

// 3유저에게 return할 형식을 지정: 레스폰스 객체
public record GlobalResponseDTO<T>(
        String code
        , String message
        , T data
) {
    // data가 있는 error
    public static <T> GlobalResponseDTO<T> from(CustomResponseCode customResponseCode, T data) {
        return new GlobalResponseDTO<T>(customResponseCode.getCode(), customResponseCode.name(), data);
    }

    // data가 없는(null) error
    public static GlobalResponseDTO<Void> from(CustomResponseCode customResponseCode) {
        return new GlobalResponseDTO<Void>(customResponseCode.getCode(), customResponseCode.name(), null);
    }

    // data가 있는 success
    public static <T> GlobalResponseDTO<T> success(T data) {
        return GlobalResponseDTO.<T>from(CustomResponseCode.SUCCESS, data);
    }

    // data가 없는 success
    public static GlobalResponseDTO<Void> success() {
        return GlobalResponseDTO.<Void>from(CustomResponseCode.SUCCESS);
    }

//    public static ResponseEntity<GlobalRes<Void>> success() {
//        return ResponseEntity.ok(new GlobalRes<Void>(CustomResponseCode.SUCCESS.getCode(), CustomResponseCode.SUCCESS.name(), null));
//    }
}
