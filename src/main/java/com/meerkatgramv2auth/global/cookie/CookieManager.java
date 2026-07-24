package com.meerkatgramv2auth.global.cookie;

import com.meerkatgramv2auth.global.jwt.JwtConfig;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CookieManager {
    private final JwtConfig jwtConfig;

    public void setRefreshTokenToCookie(HttpServletResponse response, String refreshToken) {
        this.setCookie(
                response,
                jwtConfig.refreshTokenCookieName(),
                refreshToken,
                jwtConfig.refreshTokenCookieExpiry(),
                jwtConfig.reissueUri()
        );
    }

    public void removeRefreshToken(HttpServletResponse response) {
        this.setCookie(
                response
                , jwtConfig.refreshTokenCookieName()
                , null
                , 0
                , jwtConfig.reissueUri()
        );
    }

    public Optional<String> getRefreshTokenToCookie(HttpServletRequest request) {
        return this.getCookie(request, jwtConfig.refreshTokenCookieName())
                .map(Cookie::getValue);
    }


    // Request Header에서 특정 쿠키 획득(Optional 반환)
    // request:  요청 받은 데이터
    // name: 찾고자하는 쿠키명
        // Optional 타입:
        // 반환값이 NULL이 될 경우 NullPointException이 일어날 수 있음.
        // Optional타임은 null처리를 해주지 않으면 오류가 나도록 설계됨
    public Optional<Cookie> getCookie(HttpServletRequest request, String name) {
        // 쿠키 존재여부 확인
        if(request.getCookies() == null) {
            return Optional.empty();
        }

        // 찾고자하는 name과 가지고 있는 name이 일치하는 쿠키객체(name:value) 획득
        return Arrays.stream(request.getCookies())
                .filter(cookie -> cookie.getName().equals(name))
                .findFirst();
    }

    // 쿠키 생성 메소드
    public void setCookie(HttpServletResponse response, String name, String value, int maxAge, String path) {
        Cookie cookie = new Cookie(name, value); // 해당 이름과 값으로 쿠키 인스턴스 생성
        cookie.setPath(path); // 쿠키를 사용할 path설정
        cookie.setMaxAge(maxAge); // 쿠키 유효시간 설정
        cookie.setHttpOnly(true); // HttpOnly 설정: XSS 공격 방지
        cookie.setSecure(jwtConfig.secure()); // 시큐어 설정: MITM 공격 방지(전송 중에 쿠키 탈취를 막기 위해, 암호화된 https로만 쿠키 전송)

        response.addCookie(cookie);
    }


}
