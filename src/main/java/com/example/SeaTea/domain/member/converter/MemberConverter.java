package com.example.SeaTea.domain.member.converter;

import com.example.SeaTea.domain.member.dto.request.MemberReqDTO;
import com.example.SeaTea.domain.member.dto.response.MemberResDTO;
import com.example.SeaTea.domain.member.entity.Member;

public class MemberConverter {

  // Entity -> DTO
  public static MemberResDTO.JoinDTO toJoinDTO(
      Member member
  ){
    return MemberResDTO.JoinDTO.builder()
        .id(member.getId())
        .createdAt(member.getCreatedAt())
        .build();
  }

  // DTO -> Entity
  public static Member toMember(
      MemberReqDTO.JoinDTO dto
  ){
    return Member.builder()
        .email(dto.email())
        .nickname(dto.nickname())
//        .profile_image(dto.profile_image())
        .build();
  }

  // 테스트
  public static MemberResDTO.Tasting toTestingDTO(
      String testing
  ) {
    return MemberResDTO.Tasting.builder()
        .testing(testing)
        .build();
  }

  public static MemberResDTO.Exceptions toExceptionsDTO(
      String exceptions
  ) {
    return MemberResDTO.Exceptions.builder()
        .message(exceptions)
        .build();
  }
}
