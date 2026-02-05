package com.example.SeaTea.domain.member.dto.request;

import jakarta.validation.constraints.NotBlank;

public class MemberReqDTO {

  // 회원가입 (아이디, 비번)
  public record JoinDTO(
      @NotBlank
      String email,
      @NotBlank
      String password
  ) {}

  // 회원가입 (사진, 닉네임)
  public record ProfileDTO(
      @NotBlank
      Long memberId, // 어떤 회원의 프로필을 수정할지 식별자 필요
      @NotBlank
      String nickname
      // profile_image는 파일(MultipartFile)로 따로 받을 것이므로 DTO 안에는 닉네임과 회원 식별값만 받음
  ) {}

}
