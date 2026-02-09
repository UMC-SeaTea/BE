package com.example.SeaTea.global.auth.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Role {

  // Spring Security는 기본적으로 "ROLE_" 접두사를 사용하여 권한을 식별합니다.
  ROLE_ADMIN("ROLE_ADMIN", "관리자"),
  ROLE_MEMBER("ROLE_MEMBER", "일반 회원");

  private final String key;
  private final String title;
}