package com.example.SeaTea.global.auth.controller;

import com.example.SeaTea.domain.member.dto.response.MemberResDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth", description = "인증 및 로그인 관리 API")
@RestController
@RequestMapping("/api")
public class AuthController {

  //로그인
  @Operation(
      summary = "일반 로그인",
      description = "이메일과 비밀번호를 사용하여 JWT 토큰을 발급받습니다."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "로그인 성공 (헤더 혹은 바디에 토큰 포함)"),
      @ApiResponse(responseCode = "401", description = "로그인 실패 (인증 정보 불일치)")
  })
  @PostMapping("/login")
  public void login(@RequestBody MemberResDTO.LoginRequestDTO loginRequest) {
    // 이 메서드는 JsonLoginFilter가 요청을 가로채기 때문에 실제로 실행되지 않습니다.
    // 하지만 Swagger는 이 메서드 정보를 바탕으로 문서를 생성합니다.
  }

  // 로그아웃
  @Operation(
      summary = "로그아웃",
      description = "서버 세션(있을 경우)을 무효화하고 로그아웃 처리를 합니다. 클라이언트는 저장된 JWT 토큰을 삭제해야 합니다."
  )
  @PostMapping("/logout")
  public void logout() {
    // Security 필터가 가로채기 때문에 이 메서드는 실행 X
  }
}
