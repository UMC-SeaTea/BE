package com.example.SeaTea.domain.diagnosis.service;

import com.example.SeaTea.domain.diagnosis.converter.DiagnosisQuickConverter;
import com.example.SeaTea.domain.diagnosis.dto.request.DiagnosisQuickRequestDTO;
import com.example.SeaTea.domain.diagnosis.dto.response.DiagnosisQuickResponseDTO;
import com.example.SeaTea.domain.diagnosis.entity.DiagnosisResponse;
import com.example.SeaTea.domain.diagnosis.entity.DiagnosisSession;
import com.example.SeaTea.domain.diagnosis.entity.TastingNoteType;
import com.example.SeaTea.domain.diagnosis.enums.Mode;
import com.example.SeaTea.domain.diagnosis.enums.QuickKeyword;
import com.example.SeaTea.domain.diagnosis.enums.TastingNoteTypeCode;
import com.example.SeaTea.domain.diagnosis.exception.DiagnosisException;
import com.example.SeaTea.domain.diagnosis.exception.DiagnosisErrorStatus;
import com.example.SeaTea.domain.diagnosis.repository.DiagnosisResponseRepository;
import com.example.SeaTea.domain.diagnosis.repository.DiagnosisSessionRepository;
import com.example.SeaTea.domain.diagnosis.repository.TastingNoteTypeRepository;
import com.example.SeaTea.domain.diagnosis.scoring.QuickScoring;
import com.example.SeaTea.domain.member.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
//간단 조회용
public class DiagnosisQuickService {

    private final DiagnosisSessionRepository diagnosisSessionRepository;
    private final DiagnosisResponseRepository diagnosisResponseRepository;
    private final TastingNoteTypeRepository tastingNoteTypeRepository;

    public DiagnosisQuickResponseDTO submitQuickDiagnosis(
            Member member,
            DiagnosisQuickRequestDTO req
    ) {
        // 1) 세션 생성
        DiagnosisSession session = DiagnosisSession.builder()
                .member(member)
                .mode(Mode.QUICK)
                .build();
        diagnosisSessionRepository.save(session);

        List<QuickKeyword> keywords = req.getKeywords();
        if (keywords == null || keywords.size() != 3) {
            throw new DiagnosisException(DiagnosisErrorStatus._INVALID_STEP);
        } //키워드 중간에 null이 들어감

        // 2) 응답 저장 (KW01~KW03)
        List<DiagnosisResponse> responses =
                DiagnosisQuickConverter.fromKeywords(session, keywords);
        diagnosisResponseRepository.saveAll(responses);

        // 3) 점수 계산
        Map<TastingNoteTypeCode, Integer> scores = QuickScoring.score(keywords);

        // 4) 결과 타입 결정 (동점 → 첫 번째 키워드 main)
        int maxScore = scores.values().stream()
                .mapToInt(Integer::intValue)
                .max()
                .orElse(0);

        List<TastingNoteTypeCode> candidates = scores.entrySet().stream()
                .filter(e -> e.getValue() != null && e.getValue() == maxScore)
                .map(Map.Entry::getKey)
                .toList();

        TastingNoteTypeCode resultCode =
                (candidates.size() == 1)
                        ? candidates.get(0)
                        : QuickScoring.getMainType(keywords.get(0));

        // 5) 세션에 결과 타입 반영
        TastingNoteType typeEntity =
                tastingNoteTypeRepository.findByCode(resultCode.name())
                        .orElseThrow(() ->
                                new DiagnosisException(DiagnosisErrorStatus._TYPE_NOT_FOUND)
                        );//타입 조회 실패 -> 결과는 잘 나왔는데 DB에 해당 타입이 없음 -> 서버문제

        session.updateType(typeEntity);

        // 6) 응답 DTO 반환
        return DiagnosisQuickConverter.toResponseDTO(resultCode, keywords, scores);
    }
}