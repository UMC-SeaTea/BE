package com.example.SeaTea.global.config.handler;

import com.example.SeaTea.domain.member.dto.response.MemberResDTO;
import com.example.SeaTea.domain.member.entity.Member;
import com.example.SeaTea.domain.member.exception.code.MemberSuccessCode;
import com.example.SeaTea.global.apiPayLoad.ApiResponse;
import com.example.SeaTea.global.auth.CustomUserDetails;
import com.example.SeaTea.global.auth.entity.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
public class CustomSuccessHandler implements AuthenticationSuccessHandler {

  private final JwtTokenProvider jwtTokenProvider;

  @Override
  public void onAuthenticationSuccess(
      HttpServletRequest request,
      HttpServletResponse response,
      Authentication authentication
  ) throws IOException {

    CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
    Member member = userDetails.getMember();
    boolean isNewUser = member.isNewUser();

    // 토큰 생성
    String accessToken = jwtTokenProvider.createAccessToken(authentication);

    // 리다이렉트 URI 생성 (프론트엔드가 요청한 형식)
    // ex) http://localhost:5173/oauth/callback?accessToken=eyJh...
    String targetUrl = UriComponentsBuilder.fromUriString("http://localhost:5173/oauth/callback")
        .queryParam("accessToken", accessToken)
        .queryParam("isNewUser", isNewUser)
        .build()
        .toUriString();

    // 리다이렉트
    response.sendRedirect(targetUrl);

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
