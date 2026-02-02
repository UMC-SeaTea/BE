package com.example.SeaTea.domain.member.dto.request;

import jakarta.validation.constraints.NotBlank;

public class MemberReqDTO {

  public record JoinDTO(
      @NotBlank
      String email,
      @NotBlank
      String password,
      @NotBlank
      String nickname,
      String profile_image
  ) {}
}
