package com.example.SeaTea.global.config.handler;

import com.example.SeaTea.domain.member.entity.Member;
import com.example.SeaTea.domain.member.repository.MemberRepository;
import com.example.SeaTea.global.apiPayLoad.ApiResponse;
import com.example.SeaTea.global.auth.service.CustomUserDetails;
import com.example.SeaTea.global.auth.entity.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
public class CustomSuccessHandler implements AuthenticationSuccessHandler {

  private final JwtTokenProvider jwtTokenProvider;
  private final MemberRepository memberRepository;
  private final ObjectMapper objectMapper;
//  private final RefreshTokenRepository refreshTokenRepository;

  @Value("${app.frontend-callback-url}")
  private String frontendCallbackUrl;

  @Override
  public void onAuthenticationSuccess(
      HttpServletRequest request,
      HttpServletResponse response,
      Authentication authentication
  ) throws IOException {

    Member member;
    boolean isNewUser;

    // 인증 객체에 따른 Member 나누기
    if (authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
      member = userDetails.getMember();
    } else {
      // KakaoOAuth2UserService에서 CustomUserDetails를 반환하여 if문 통과
      throw new IllegalStateException("알 수 없는 사용자 타입입니다.");
    }

    isNewUser = member.isNewUser();

    // Access 토큰 생성
    String accessToken = jwtTokenProvider.createAccessToken(authentication);

    // Refresh 토큰 생성
    String refreshToken = jwtTokenProvider.createRefreshToken(authentication);

    // Refresh Token을 DB나 Redis에 저장 (선택 사항이지만 보안상 필수)
    // refreshTokenRepository.save(new RefreshToken(member.getId(), refreshToken));

    /*
    // 리다이렉트 시 쿠키에 Refresh Token 심기
    Cookie refreshCookie = new Cookie("refreshToken", refreshToken);
    refreshCookie.setHttpOnly(true);  // 자바스크립트가 접근 못하게 막음 (XSS 방지)
    refreshCookie.setSecure(true);    // HTTPS에서만 전송 (로컬 개발시에는 false로 테스트 가능)
    refreshCookie.setPath("/");       // 모든 경로에서 쿠키 전송
    refreshCookie.setMaxAge(7 * 24 * 60 * 60); // 7일 (초 단위)
    // 응답에 쿠키 추가
    response.addCookie(refreshCookie);
    */

    boolean isProduction = true; // 환경 변수나 프로필로 관리할 때, false

    org.springframework.http.ResponseCookie refreshCookie = org.springframework.http.ResponseCookie
        .from("refreshToken", refreshToken)
        .httpOnly(true)
        .secure(isProduction) // 운영 환경(HTTPS)에서만 true
        .path("/")
        .maxAge(14 * 24 * 60 * 60) // 14일 초 단위
        .sameSite(isProduction ? "None" : "Lax") // 크로스 도메인 설정 시 None 필요
        .build();

    response.addHeader(org.springframework.http.HttpHeaders.SET_COOKIE, refreshCookie.toString());

    // 신규 회원인 것 확인 후 false로 전환
    if (isNewUser) {
      member.offNewUser();
      memberRepository.save(member); // DB에 즉시 반영
    }

    // 요청 헤더나 세션을 통해 소셜 로그인인지 확인 (예: OAuth2AuthenticationToken 체크)
    if (authentication instanceof OAuth2AuthenticationToken) {
      // 소셜 로그인은 기존처럼 리다이렉트
      String targetUrl = UriComponentsBuilder.fromUriString(frontendCallbackUrl)
          .queryParam("accessToken", accessToken)
          .queryParam("isNewUser", isNewUser)
          .queryParam("nickname", member.getNickname())
          .queryParam("profileImage", member.getProfile_image())
          .build()
          .encode(StandardCharsets.UTF_8)
          .toUriString();
      response.sendRedirect(targetUrl);
    } else {
      // 일반 JSON 로그인은 JSON 바디로 응답 (기존 주석 처리했던 로직 활용)
      response.setStatus(HttpServletResponse.SC_OK);
      response.setContentType("application/json;charset=UTF-8");

      // JSON 응답 생성 (accessToken 포함)
      Map<String, Object> body = new java.util.HashMap<>();
      body.put("accessToken", accessToken);
      body.put("isNewUser", isNewUser);
      body.put("nickname", member.getNickname() != null ? member.getNickname() : "");
      body.put("profileImage", member.getProfile_image() != null ? member.getProfile_image() : "");
      objectMapper.writeValue(response.getWriter(), ApiResponse.onSuccess(body));
    }
  }
}
