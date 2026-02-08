package com.example.SeaTea.global.config.handler;

import com.example.SeaTea.domain.member.exception.code.MemberErrorCode;
import com.example.SeaTea.global.apiPayLoad.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomFailureHandler implements AuthenticationFailureHandler {

  private final ObjectMapper objectMapper;

  @Override
  public void onAuthenticationFailure(
      HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException exception
  ) throws IOException {

    response.setStatus(HttpStatus.UNAUTHORIZED.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding("UTF-8");

    System.out.println("로그인 실패 이유: " + exception.getMessage());

    objectMapper.writeValue(
        response.getWriter(),
        ApiResponse.onFailure(MemberErrorCode._LOGIN_FAILED.getCode(),MemberErrorCode._LOGIN_FAILED.getMessage(), MemberErrorCode._LOGIN_FAILED)
    );
  }

}
