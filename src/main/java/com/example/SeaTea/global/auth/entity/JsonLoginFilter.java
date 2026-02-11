package com.example.SeaTea.global.auth.entity;

import com.example.SeaTea.domain.member.dto.response.MemberResDTO.LoginRequestDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.io.IOException;

public class JsonLoginFilter extends AbstractAuthenticationProcessingFilter {

  private final ObjectMapper objectMapper = new ObjectMapper();

  public JsonLoginFilter() {
    super(AntPathRequestMatcher.antMatcher(HttpMethod.POST, "/api/login"));
  }

  @Override
  public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
      throws AuthenticationException, IOException {

    String contentType = request.getContentType();

    // HTTP 메서드 및 Content-Type 검증
    if (!request.getMethod().equals("POST") ||
        contentType == null ||
        !contentType.contains("application/json")) {
      throw new AuthenticationServiceException("지원되지 않는 인증 방식입니다.");
    }

    try{
      // 바디(InputStream)를 LoginRequest 객체로 매핑
       LoginRequestDTO loginRequest = objectMapper.readValue(request.getInputStream(), LoginRequestDTO.class);

      if (loginRequest == null) {
        throw new AuthenticationServiceException("로그인 요청 데이터가 올바르지 않습니다.");
      }

      if (loginRequest.email() == null || loginRequest.password() == null) {
        throw new AuthenticationServiceException("이메일과 비밀번호는 필수입니다.");
      }

      // 인증을 위한 토큰 생성 (Principal: 이메일, Credentials: 비밀번호)
      UsernamePasswordAuthenticationToken authToken =
          new UsernamePasswordAuthenticationToken(loginRequest.email(), loginRequest.password());

      // AuthenticationManager에게 인증 위임
      return this.getAuthenticationManager().authenticate(authToken);
    } catch (Exception e) {
      // Jackson 매핑 실패 시 발생하는 에러를 캐치
      throw new AuthenticationServiceException("로그인 데이터 파싱 실패: " + e.getMessage());
    }
  }
}