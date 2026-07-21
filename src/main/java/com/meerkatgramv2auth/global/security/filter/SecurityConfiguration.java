package com.meerkatgramv2auth.global.security.filter;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

// @EnableMethodSecurity: 메소드레벨(컨트롤러)에서 권한 제어를 활성화. 시큐리티: filter 단계를 거쳐서 servlet(컨트롤러...서비스) 단으로 넘어감.
// 화이트리스트 방식일 때는 filter에서 거르는 방식이 편리 / 블랙리스트 방식일 때는 servlet에서 거르는 것이 편리
@EnableMethodSecurity
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

    // 비번 암호화(인코더)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 시큐리티 설정
        // HttpSecurity: : 웹 요청 보안, 세션 관리, 인증/인가 규칙 등 스프링 시큐리티의 핵심 보안 설정을 Builder 방식으로 구성하게 해주는 객체
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .sessionManagement(sesion -> sesion.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 세션 비활성화(세션: 서버가 사용자의 로그인 상태나 정보를 메모리에 기억해 두는 공간)
                .httpBasic(AbstractHttpConfigurer::disable)  // HTTP Basic 인증 비활성화(HTTP Basic 인증: Base64를 사용한 기본적인 인증방식)
                .formLogin(AbstractHttpConfigurer::disable)  // 폼로그인 기능 비활성화(서버가 직접 HTML 로그인 폼 화면을 제공하는 기능)
                .csrf(AbstractHttpConfigurer::disable)  // CSRF 토큰 인증 비활성화(CSRF: 공격자가 희생자의 권한을 도용하여 특정 웹 사이트의 기능을 실행하도록하는 보안 공격)
                .authorizeHttpRequests(request -> request.anyRequest().permitAll())  // 인증 여부와 무관하게 모든 요청 통과
                .build();
    }
}
