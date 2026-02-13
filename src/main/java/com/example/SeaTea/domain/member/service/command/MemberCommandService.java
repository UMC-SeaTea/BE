package com.example.SeaTea.domain.member.service.command;

import com.example.SeaTea.domain.member.dto.request.MemberReqDTO;
import com.example.SeaTea.domain.member.dto.response.MemberResDTO;
import com.example.SeaTea.domain.member.entity.Member;
import jakarta.servlet.http.HttpServletResponse;

public interface MemberCommandService {

  MemberResDTO.JoinDTO signup(
      MemberReqDTO.JoinDTO dto
  );

  MemberResDTO.UpdateNicknameResultDTO updateNickname(Member member, MemberReqDTO.UpdateNicknameDTO dto);

  MemberResDTO.UpdateProfileImageResultDTO updateProfileImage(Member member, MemberReqDTO.UpdateProfileImageDTO dto);

  void checkEmailDuplication(String email);
  void checkNicknameDuplication(String nickname);

  boolean isNicknameDuplicated(String nickname);

  // 탈퇴 메서드
  void withdraw(Member member);

  // 토큰 재발급
  String reissue(String refreshToken, HttpServletResponse response);
}
