package com.example.SeaTea.domain.member.converter;

import com.example.SeaTea.domain.member.dto.response.MemberResDTO;

public class MemberConverter {

  // 테스트
  public static MemberResDTO.Tasting toTestingDTO(
      String testing
  ) {
    return MemberResDTO.Tasting.builder()
        .testing(testing)
        .build();
  }
}
