package com.example.SeaTea.domain.member.service.command;

import com.example.SeaTea.domain.member.dto.request.MemberReqDTO;
import com.example.SeaTea.domain.member.dto.response.MemberResDTO;

public interface MemberCommandService {

  MemberResDTO.JoinDTO signup(
      MemberReqDTO.JoinDTO dto,
      MemberReqDTO.ProfileDTO profDto
  );


}
