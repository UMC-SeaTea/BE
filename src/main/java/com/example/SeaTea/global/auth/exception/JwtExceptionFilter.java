package com.example.SeaTea.global.auth.exception;

import com.example.SeaTea.domain.member.exception.code.MemberErrorCode;
import com.example.SeaTea.global.apiPayLoad.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;
import io.jsonwebtoken.JwtException;
import java.io.IOException;

@RequiredArgsConstructor
public class JwtExceptionFilter extends OncePerRequestFilter {
  private final ObjectMapper objectMapper;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    try {
      // 다음 필터(JwtAuthenticationFilter) 실행
      filterChain.doFilter(request, response);
    } catch (JwtException e) {
      // JWT 관련 에러가 던져지면 캐치해서 응답 생성
      setErrorResponse(response, e.getMessage());
    }
  }

  private void setErrorResponse(HttpServletResponse response, String message) throws IOException {
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding("UTF-8");

    ApiResponse<Object> errorResponse = ApiResponse.onFailure(MemberErrorCode._JWT_WRONG.getCode(), MemberErrorCode._JWT_WRONG.getMessage() , null);

    objectMapper.writeValue(response.getWriter(), errorResponse);
  }

}
