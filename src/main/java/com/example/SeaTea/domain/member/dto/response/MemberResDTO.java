package com.example.SeaTea.domain.member.dto.response;

import lombok.Builder;
import lombok.Getter;

public class MemberResDTO {

  // 테스트
  @Builder
  @Getter
  public static class Tasting {
    private String testing;
  }
}
