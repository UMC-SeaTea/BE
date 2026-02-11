package com.example.SeaTea.domain.member.service.query;

import com.example.SeaTea.domain.member.dto.response.MemberResDTO;

public interface MemberQueryService {
  void checkFlag(Long flag);
  MemberResDTO.MemberInfoDTO getMemberInfo(Long memberId);
}
