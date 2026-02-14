package com.example.SeaTea.global.auth.exception;

import com.example.SeaTea.domain.member.exception.MemberException;
import com.example.SeaTea.domain.member.exception.code.MemberErrorCode;
import com.example.SeaTea.global.apiPayLoad.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import io.jsonwebtoken.JwtException;
import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtExceptionFilter extends OncePerRequestFilter {
  private final ObjectMapper objectMapper;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    try {
      // 다음 필터(JwtAuthenticationFilter) 실행
      filterChain.doFilter(request, response);
    } catch (MemberException e) {
      // 우리가 정의한 에러 코드 활용omm
      setErrorResponse(response, e.getCode().getReason().getCode(), e.getCode().getReason().getMessage());
    } catch (ExpiredJwtException e) {
      // 토큰 만료
      setErrorResponse(response, MemberErrorCode._JWT_WRONG.getReason().getCode(), "토큰이 만료되었습니다.");
    } catch (JwtException | IllegalArgumentException e) {
      // 그 외 JWT 관련 오류
      setErrorResponse(response, MemberErrorCode._JWT_WRONG.getReason().getCode(), MemberErrorCode._JWT_WRONG.getReason().getMessage());
    }
  }

  private void setErrorResponse(HttpServletResponse response, String code, String message) throws IOException {
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding("UTF-8");

    ApiResponse<Object> errorResponse = ApiResponse.onFailure(code, message, null);

    objectMapper.writeValue(response.getWriter(), errorResponse);
  }

}
