package com.meerkatgramv2auth.global.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;


// 추상 클래스 OncePerRequestFilter를 상속, doFilterInternal 메소드 구현 필요
@Component
public class HeaderAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        // Gateway에서 토큰이 정상적인지 검증
        // 시큐리티에서 req 권한 제안하지 않는 대신,
        // req 헤더로부터 userId와 role 정보 받아서 정상 존재하는지 확인(Gateway에서 토큰이 정상적인지 검증)
        String userId = request.getHeader("X-User-Id");
        String role = request.getHeader("X-User-role");

        // StringUtils.isNotBlank(): ()가 null 혹은 빈 문자열 이 아닐 것
        if(StringUtils.isNotBlank(userId) && StringUtils.isNotBlank(role)) {
            // 스프링 시큐리티가 사용할, 유저 정보를 담은 "인증 토큰" 생성
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    userId  // userId
                    , null  // 민감한 정보
                    , List.of(new SimpleGrantedAuthority("ROLE_" + role))  // 유저의 role(여러개 가지고 있을 수 있으니 List), 스프링 시큐리티가 이해할 수 있는 개체 형식으로 만들어줌 "ROLE_롤"
            );

            // 위에서 생성한 토큰을 스프링 시큐리티에 등록
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        //
        // filterChain: 이번 필터링은 끝났으니, 다음 필터링으로 넘어 가라
        filterChain.doFilter(request, response);
    }
}
