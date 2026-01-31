package com.example.SeaTea.domain.member.dto.request;

public class MemberReqDTO {

  public record JoinDTO(
      String email,
      String nickname,
      String profile_image
  ) {}
}
