package com.example.SeaTea.global.config.handler;

import com.example.SeaTea.domain.member.entity.Member;
import com.example.SeaTea.domain.member.repository.MemberRepository;
import com.example.SeaTea.global.apiPayLoad.ApiResponse;
import com.example.SeaTea.global.auth.CustomUserDetails;
import com.example.SeaTea.global.auth.entity.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
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

  @Value("${app.frontend-callback-url}")
  private String frontendCallbackUrl;

  @Override
  public void onAuthenticationSuccess(
      HttpServletRequest request,
      HttpServletResponse response,
      Authentication authentication
  ) throws IOException {

//    CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
//    Member member = userDetails.getMember();
//    boolean isNewUser = member.isNewUser();

    Member member;
    boolean isNewUser;

    // 인증 객체에 따른 Member 나누기
    if (authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
      member = userDetails.getMember();
    } else {
      // 소셜 로그인 시 OAuth2User를 처리하는 로직 (프로젝트 구조에 따라 조정)
      // 만약 KakaoOAuth2UserService에서 CustomUserDetails를 반환하게 설정했다면 위 if문으로 다 해결됩니다.
      // 아니라면 여기서 DB 조회가 필요할 수 있습니다. (speculation)
      throw new IllegalStateException("알 수 없는 사용자 타입입니다.");
    }

    isNewUser = member.isNewUser();

    // 토큰 생성
    String accessToken = jwtTokenProvider.createAccessToken(authentication);

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
          .build().toUriString();
      response.sendRedirect(targetUrl);
    } else {
      // 일반 JSON 로그인은 JSON 바디로 응답 (기존 주석 처리했던 로직 활용)
      response.setStatus(HttpServletResponse.SC_OK);
      response.setContentType("application/json;charset=UTF-8");

      // JSON 응답 생성 (accessToken 포함)
      Map<String, Object> body = Map.of("accessToken", accessToken, "isNewUser", isNewUser);
      objectMapper.writeValue(response.getWriter(), ApiResponse.onSuccess(body));
//      String jsonResponse = String.format("{\"accessToken\": \"%s\", \"isNewUser\": %b}", accessToken, isNewUser);
//      response.getWriter().write(jsonResponse);
    }

    // 리다이렉트 URI 생성 (프론트엔드가 요청한 형식)
    // ex) http://localhost:5173/oauth/callback?accessToken=eyJh...
//    String targetUrl = UriComponentsBuilder.fromUriString(frontendCallbackUrl)
//        .queryParam("accessToken", accessToken)
//        .queryParam("isNewUser", isNewUser)
//        .build()
//        .toUriString();

    // 리다이렉트
//    response.sendRedirect(targetUrl);

//    CustomUserDetails userDetails =
//        (CustomUserDetails) authentication.getPrincipal();
//
//    Member member = userDetails.getMember(); // getter 필요
//
//    MemberResDTO.LoginDTO dto = MemberResDTO.LoginDTO.builder()
//        .id(member.getId())
//        .email(member.getEmail())
//        .role(member.getRole().name())
//        .build();
//
//    response.setStatus(HttpStatus.OK.value());
//    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
//    response.setCharacterEncoding("UTF-8");
//
//    objectMapper.writeValue(
//        response.getWriter(),
//        ApiResponse.of(MemberSuccessCode._LOGIN_SUCCESS, dto)
//    );
  }
}
