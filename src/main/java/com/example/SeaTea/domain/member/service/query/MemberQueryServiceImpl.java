package com.example.SeaTea.domain.member.service.query;

import com.example.SeaTea.domain.member.exception.MemberException;
import com.example.SeaTea.domain.member.exception.code.MemberErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberQueryServiceImpl implements MemberQueryService {

  @Override
  public void checkFlag(Long flag) {
    if(Long.valueOf(1L).equals(flag)){
      throw new MemberException(MemberErrorCode._NOT_FOUND);
    }
  }
}
