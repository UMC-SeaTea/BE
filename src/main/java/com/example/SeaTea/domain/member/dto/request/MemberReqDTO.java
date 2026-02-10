package com.example.SeaTea.domain.member.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class MemberReqDTO {

  // 회원가입 (아이디, 비번)
  public record JoinDTO(
      @NotBlank
      String email,
      @NotBlank
      String password,
      @NotBlank
      String passwordConfirm,
      @NotBlank
      String nickname,
      String profile_url
  ) {}

  // 닉네임 업데이트
  public record UpdateNicknameDTO(
      @NotBlank
      @Size(min = 2, max = 10)
      String newNickname
  ){}

}
