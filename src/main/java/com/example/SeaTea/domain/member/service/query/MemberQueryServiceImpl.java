package com.example.SeaTea.domain.member.service.query;

import com.example.SeaTea.domain.diagnosis.entity.DiagnosisSession;
import com.example.SeaTea.domain.diagnosis.entity.TastingNoteType;
import com.example.SeaTea.domain.diagnosis.repository.DiagnosisSessionRepository;
import com.example.SeaTea.domain.member.converter.MemberConverter;
import com.example.SeaTea.domain.member.dto.response.MemberResDTO;
import com.example.SeaTea.domain.member.entity.Member;
import com.example.SeaTea.domain.member.exception.MemberException;
import com.example.SeaTea.domain.member.exception.code.MemberErrorCode;
import com.example.SeaTea.domain.member.repository.MemberRepository;
import com.example.SeaTea.domain.place.repository.MemberSavedPlaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberQueryServiceImpl implements MemberQueryService {

  private final MemberRepository memberRepository;
  private final MemberSavedPlaceRepository memberSavedPlaceRepository;
  private final DiagnosisSessionRepository diagnosisSessionRepository;

  @Override
  public void checkFlag(Long flag) {
    if(Long.valueOf(1L).equals(flag)){
      throw new MemberException(MemberErrorCode._NOT_FOUND);
    }
  }

  @Override
  public MemberResDTO.MemberInfoDTO getMemberInfo(Long memberId) {
    Member member = memberRepository.findById(memberId)
        .orElseThrow(() -> new MemberException(MemberErrorCode._NOT_FOUND));

    Long savedCount = memberSavedPlaceRepository.countByMember_Id(memberId);

    DiagnosisSession latestSession = diagnosisSessionRepository
        .findTopByMemberAndTypeIsNotNullOrderByCreatedAtDesc(member)
        .orElse(null);

    TastingNoteType latestType = (latestSession != null) ? latestSession.getType() : null;

    return MemberConverter.toMemberInfoDTO(member, savedCount, latestType);
  }
}
