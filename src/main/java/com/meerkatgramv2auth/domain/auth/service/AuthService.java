package com.meerkatgramv2auth.domain.auth.service;

import com.meerkatgramv2auth.domain.auth.repository.AuthRepository;
import com.meerkatgramv2auth.domain.auth.request.LoginRequestDTO;
import com.meerkatgramv2auth.domain.auth.response.AuthResponseDTO;
import com.meerkatgramv2auth.domain.user.entity.User;
import com.meerkatgramv2auth.global.cookie.CookieManager;
import com.meerkatgramv2auth.global.error.custom.NotRegisteredException;
import com.meerkatgramv2auth.global.jwt.JwtConfig;
import com.meerkatgramv2auth.global.jwt.JwtProvider;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthRepository authRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final CookieManager cookieManager;
    private final JwtConfig jwtConfig;

    // email로 유저 정보를 획득 & (.orElseThrow: null인지 체크)유저 가입 여부를 체크
    @Transactional(rollbackFor = Exception.class)
    public AuthResponseDTO login(HttpServletResponse response, LoginRequestDTO loginRequestDTO) {
        User user = authRepository.findByEmail(loginRequestDTO.email())
                .orElseThrow(() -> new NotRegisteredException("아이디와 비밀번호를 확인해주세요."));

        // 비밀번호 체크: .matches(req 비번, DB 비번)
        if(!passwordEncoder.matches(loginRequestDTO.password(), user.getPassword())) {
            throw new NotRegisteredException(("아이디와 비밀번호를 확인해주세요."));
        }

        return this.generateAuthentication(response, user);
    }

    private AuthResponseDTO generateAuthentication(HttpServletResponse response, User user) {
        // 토큰 생성
        String accessToken = jwtProvider.generateAccessToken(user);
        String refreshToken = jwtProvider.generateAccessToken(user);

        // 리프래시토큰 DB 저장 처리
        user.setRefreshToken(refreshToken);
        authRepository.save(user);

        // 리프레시토큰 cookie에 저장
        cookieManager.setCookie(response, jwtConfig.refreshTokenCookieName(), refreshToken, jwtConfig.refreshTokenExpiry(), jwtConfig.reissueUri());

        return AuthResponseDTO.from(user, accessToken);
    }
}
