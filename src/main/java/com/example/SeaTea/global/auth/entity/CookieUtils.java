package com.example.SeaTea.global.auth.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.util.SerializationUtils;
import java.util.Base64;
import java.util.Optional;

public class CookieUtils {
  private static final ObjectMapper objectMapper = new ObjectMapper();

  public static Optional<Cookie> getCookie(HttpServletRequest request, String name) {
    Cookie[] cookies = request.getCookies();
    if (cookies != null) {
      for (Cookie cookie : cookies) {
        if (cookie.getName().equals(name)) return Optional.of(cookie);
      }
    }
    return Optional.empty();
  }

  public static void addCookie(HttpServletResponse response, String name, String value, int maxAge) {
    org.springframework.http.ResponseCookie cookie = org.springframework.http.ResponseCookie.from(name, value)
        .path("/")
        .httpOnly(true)
        .secure(true)    // HTTPS 환경 필수
        .maxAge(maxAge)
        .sameSite("Lax") // CSRF 방지 및 일반적인 사용성 보장
        .build();

    response.addHeader(org.springframework.http.HttpHeaders.SET_COOKIE, cookie.toString());
  }

  public static void deleteCookie(HttpServletRequest request, HttpServletResponse response, String name) {
    Cookie[] cookies = request.getCookies();
    if (cookies != null) {
      for (Cookie cookie : cookies) {
        if (cookie.getName().equals(name)) {
          cookie.setValue("");
          cookie.setPath("/");
          cookie.setMaxAge(0);
          response.addCookie(cookie);
        }
      }
    }
  }

  // 객체를 JSON 문자열로 변환 후 Base64 인코딩
  public static String serialize(Object object) {
    try {
      return Base64.getUrlEncoder()
          .encodeToString(objectMapper.writeValueAsBytes(object));
    } catch (JsonProcessingException e) {
      throw new RuntimeException("쿠키 직렬화 실패: " + e.getMessage());
    }
  }

  // Base64 디코딩 후 JSON 문자열을 객체로 역직렬화
  public static <T> T deserialize(Cookie cookie, Class<T> cls) {
    try {
      byte[] decodedBytes = Base64.getUrlDecoder().decode(cookie.getValue());
      return objectMapper.readValue(decodedBytes, cls);
    } catch (Exception e) {
      // 💡 변조된 쿠키가 들어올 경우 예외를 처리하여 보안 공격을 무력화합니다.
      return null;
    }
  }
}
