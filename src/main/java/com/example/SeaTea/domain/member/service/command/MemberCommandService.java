package com.example.SeaTea.domain.member.service.command;

import com.example.SeaTea.domain.member.dto.request.MemberReqDTO;
import com.example.SeaTea.domain.member.dto.response.MemberResDTO;
import com.example.SeaTea.domain.member.entity.Member;

public interface MemberCommandService {

  MemberResDTO.JoinDTO signup(
      MemberReqDTO.JoinDTO dto
  );

  MemberResDTO.UpdateNicknameResultDTO updateNickname(Member member, MemberReqDTO.UpdateNicknameDTO dto);

  void checkEmailDuplication(String email);
  void checkNicknameDuplication(String nickname);
}
