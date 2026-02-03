package com.example.SeaTea.domain.member.dto.response;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

public class MemberResDTO {

  // 회원가입
  @Builder
  public record JoinDTO(
      Long id,
      LocalDateTime createdAt
  ){}

  // 로그인 (로그인 전용 / 조회 전용 분리 -> 확장)
  @Builder
  public record LoginDTO(
      Long id,
      String email,
      String password,
      String role,
      String profile_image
  ){}

  // 테스트
  @Builder
  @Getter
  public static class Tasting {
    private String testing;
  }

  @Builder
  @Getter
  public static class Exceptions {
    private String message;
  }

  @Builder
  @Getter
  public static class MemberNotFound {}
}
