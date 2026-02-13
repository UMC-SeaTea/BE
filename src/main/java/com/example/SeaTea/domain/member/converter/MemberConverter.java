package com.example.SeaTea.domain.member.converter;

import com.example.SeaTea.domain.diagnosis.entity.TastingNoteType;
import com.example.SeaTea.domain.member.dto.request.MemberReqDTO;
import com.example.SeaTea.domain.member.dto.response.MemberResDTO;
import com.example.SeaTea.domain.member.entity.Member;
import com.example.SeaTea.global.auth.enums.Role;
import java.time.LocalDateTime;

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

  public static MemberResDTO.LoginDTO toLoginDTO(Member member) {
    return MemberResDTO.LoginDTO.builder()
        .id(member.getId())
        .email(member.getEmail())
        .nickname(member.getNickname())
        .role(member.getRole().toString()) // Enum을 String으로 변환
        .profile_image(member.getProfile_image())
        .build();
  }

  // DTO -> Entity
  public static Member toMember(
      MemberReqDTO.JoinDTO dto,
      String password,
      Role role
  ){
    return Member.builder()
        .email(dto.email())
        .password(password)
        .role(role)
        .nickname(dto.nickname())
//        .profile_image(dto.profile_image())
        .build();
  }

  // 닉네임 변경
  public static MemberResDTO.UpdateNicknameResultDTO toUpdateNicknameResultDTO(Member member) {
    return MemberResDTO.UpdateNicknameResultDTO.builder()
        .id(member.getId())
        .nickname(member.getNickname())
        .updatedAt(LocalDateTime.now())
        .build();
  }

  // 프사 변경
  public static MemberResDTO.UpdateProfileImageResultDTO toUpdateProfileImageResultDTO(Member member) {
    return MemberResDTO.UpdateProfileImageResultDTO.builder()
        .id(member.getId())
        .profileImageUrl(member.getProfile_image())
        .updatedAt(LocalDateTime.now())
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

  public static MemberResDTO.MemberInfoDTO toMemberInfoDTO(Member member, Long savedCount, TastingNoteType type) {
    return MemberResDTO.MemberInfoDTO.builder()
        .userId(member.getId())
        .email(member.getEmail())
        .nickname(member.getNickname())
        .profileImageUrl(member.getProfile_image())
        .role(member.getRole())
        .savedSpaceCount(savedCount)
        .currentType(toTastingTypeDTO(type)) // 유형 변환 호출
        .createdAt(member.getCreatedAt())
        .updatedAt(member.getUpdatedAt())
        .build();
  }

  private static MemberResDTO.TastingTypeDTO toTastingTypeDTO(TastingNoteType type) {
    if (type == null) return null; // 진단 전이면 null 반환

    return MemberResDTO.TastingTypeDTO.builder()
        .id(type.getId())
        .code(type.getCode())
        .displayName(type.getDisplayName())
        .subtitle(type.getSubtitle())
        .description(type.getDescription())
        .imageUrl(type.getImageUrl())
        .createdAt(type.getCreatedAt())
        .build();
  }

  public static MemberResDTO.TokenDTO toTokenDTO(String accessToken) {
    return MemberResDTO.TokenDTO.builder()
        .accessToken(accessToken)
        .build();
  }
}
