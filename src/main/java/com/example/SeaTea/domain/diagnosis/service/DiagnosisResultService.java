package com.example.SeaTea.domain.diagnosis.service;

import com.example.SeaTea.domain.diagnosis.dto.response.DiagnosisHistoryResponseDTO;
import com.example.SeaTea.domain.diagnosis.dto.response.DiagnosisResultResponseDTO;
import com.example.SeaTea.domain.diagnosis.entity.DiagnosisSession;
import com.example.SeaTea.domain.diagnosis.entity.TastingNoteType;
import com.example.SeaTea.domain.diagnosis.exception.DiagnosisException;
import com.example.SeaTea.domain.diagnosis.exception.DiagnosisErrorStatus;
import com.example.SeaTea.domain.diagnosis.repository.DiagnosisSessionRepository;
import com.example.SeaTea.domain.member.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
//결과 조회용
public class DiagnosisResultService {

    private final DiagnosisSessionRepository diagnosisSessionRepository;

    // 나의 최근 진단 결과 (완료된 세션 중 가장 최근 1건)
    public DiagnosisResultResponseDTO getMyLatestResult(Member member) {
        // 완료된 진단 세션이 없는 경우 → 조회 결과 없음
        DiagnosisSession latest = diagnosisSessionRepository
                .findTopByMemberAndDeletedAtIsNullAndTypeIsNotNullOrderByCreatedAtDesc(member)
                .orElseThrow(() -> new DiagnosisException(DiagnosisErrorStatus._NO_COMPLETED_DIAGNOSIS));

        // 세션에 연결된 타입이 null인 경우 → 데이터 세팅 문제(서버 오류)
        if (latest.getType() == null) {
            throw new DiagnosisException(DiagnosisErrorStatus._TYPE_NOT_FOUND);
        }

        TastingNoteType type = latest.getType(); // type != null 보장
        return DiagnosisResultResponseDTO.from(type);
    }

    // 나의 진단 결과 히스토리(완료된 세션 전체) (슬라이스)
    @Transactional(readOnly = true)
    public Slice<DiagnosisHistoryResponseDTO> getMyHistory(
            Member member,
            Pageable pageable
    ) {
        Slice<DiagnosisSession> slice =
                diagnosisSessionRepository.findByMemberAndDeletedAtIsNullAndTypeIsNotNullOrderByCreatedAtDesc(
                        member,
                        pageable
                );

        return slice.map(DiagnosisHistoryResponseDTO::from);
    }
}
