package com.example.SeaTea.domain.member.dto.response;

import com.example.SeaTea.domain.member.entity.Member;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
      // 보안을 위해 비밀번호 포함 X
      //String password,
      Long id,
      String email,
      String nickname,
      String role,
      String profile_image
  ){}

  @Builder
  public static record LoginRequestDTO(
      String email,
      String password
  ){}

  // 닉네임 업데이트 결과
  @Builder
  public record UpdateNicknameResultDTO(
      Long id,
      String nickname,
      LocalDateTime updatedAt
  ){}

  // 이미지 업데이트 결과
  @Builder
  public record UpdateProfileImageResultDTO(
      Long id,
      String profileImageUrl,
      LocalDateTime updatedAt
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
