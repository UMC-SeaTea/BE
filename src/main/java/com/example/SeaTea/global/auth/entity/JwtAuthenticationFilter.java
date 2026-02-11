package com.example.SeaTea.global.auth.entity;

import io.jsonwebtoken.JwtException;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtTokenProvider jwtTokenProvider;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    try {
      String token = resolveToken(request);
      if (StringUtils.hasText(token)) {
        jwtTokenProvider.validateToken(token); // 여기서 예외가 발생하면 ExceptionFilter로 이동
        Authentication auth = jwtTokenProvider.getAuthentication(token);
        SecurityContextHolder.getContext().setAuthentication(auth);
      }
      filterChain.doFilter(request, response);
    } catch (JwtException e) {
      // 이 예외는 상위 필터인 JwtExceptionFilter에서 잡음
      throw e;
    }
  }

//      throws ServletException, IOException {
//
//    // 요청 헤더에서 JWT 토큰 추출
//    String token = resolveToken(request);
//
//    // validateToken으로 토큰 유효성 검사
//    if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
//      // 토큰이 유효하면 인증 객체(Authentication) 생성
//      Authentication authentication = jwtTokenProvider.getAuthentication(token);
//      // 3. SecurityContext에 인증 정보 저장 (로그인 처리 완료)
//      SecurityContextHolder.getContext().setAuthentication(authentication);
//    }
//
//    // 4. 다음 필터로 이동
//    filterChain.doFilter(request, response);
//  }

  // 헤더에서 "Bearer " 부분을 제외한 토큰 값만 추출
  private String resolveToken(HttpServletRequest request) {
    String bearerToken = request.getHeader("Authorization");
    if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
      return bearerToken.substring(7);
    }
    return null;
  }

}
