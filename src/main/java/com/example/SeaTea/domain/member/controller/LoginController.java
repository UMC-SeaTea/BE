package com.example.SeaTea.domain.member.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Getter;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

// swagger에서 /api/login 엔드포인트를 보이게 하기 위한 더미 컨트롤러

@RestController
@Tag(name = "Auth", description = "인증 관련 API")
public class LoginController {
  @Operation(summary = "로그인", description = "이메일과 비밀번호를 이용해 로그인합니다. Swagger로 로그인 불가 POSTMAN으로 진행!")
  @PostMapping("/api/login")
  public void login(@RequestBody LoginRequest loginRequest) {
    // 실제로는 Spring Security 필터가 가로채기 때문에 이 메서드는 실행되지 않습니다.
    throw new IllegalStateException("이 메서드는 Spring Security 필터에 의해 가로채어집니다.");
  }
}

@Getter
class LoginRequest {
  private String email;
  private String password;
}