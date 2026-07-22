package com.meerkatgramv2auth.global.jwt;

import com.meerkatgramv2auth.domain.user.entity.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtProvider {
    private final JwtConfig jwtConfig;
    private final SecretKey secretKey;

    // 생성자 커스텀
        // Keys.hmacShaKeyFor() : 토큰의 signature 부분의 비밀키를 만드는(암호화하는) 메소드
        // Decoders.BASE64.decode(): 암호화된 부분을 디코딩하는 작업
    public JwtProvider(JwtConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtConfig.secret()));
    }

    // 엑세스토큰 생성(generateToken을 가져와 사용)
    public String generateAccessToken(User user) {
        return this.generateToken(user, jwtConfig.accessTokenExpiry());
    }

    // 리프레시토큰 생성(generateToken을 가져와 사용)
    public String generateRefreshToken(User user) {
        return this.generateToken(user, jwtConfig.refreshTokenExpiry());
    }

    // jwt 형식의 토큰 생성
    private String generateToken(User user, int expiry) {
        Date now = new Date();

        // jwt 커스텀
        return Jwts.builder()
                .header() // 지금부터 헤더를 셋팅하겠다
                .type(jwtConfig.type())  // 토큰 유형
                .and()  // 헤더 끝, 다시 builder()로 돌아간다
                .subject(String.valueOf(user.getId()))  // subject: 해당 토큰의 대상이 되는 주인(유저) 지정(여기서는 파라미터로 받은 유저ID를 주인으로 지정. lon
                .issuer(jwtConfig.issuer()) // 토큰 발급자 셋팅
                .issuedAt(now)  // 발급 시간 셋팅
                .expiration(new Date(now.getTime() + expiry))  // 토큰 만료시간 설정(현재시간 + JWT 콘피그 시간)
                .claim("role", user.getRole())  // private claim: 커스텀 속성 설정
                .signWith(secretKey)  // 토큰의 signature 부분 셋팅
                .compact();
    }
}
