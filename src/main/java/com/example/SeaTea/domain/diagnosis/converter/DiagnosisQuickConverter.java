package com.example.SeaTea.domain.diagnosis.converter;

import com.example.SeaTea.domain.diagnosis.dto.response.DiagnosisQuickResponseDTO;
import com.example.SeaTea.domain.diagnosis.entity.DiagnosisResponse;
import com.example.SeaTea.domain.diagnosis.entity.DiagnosisSession;
import com.example.SeaTea.domain.diagnosis.enums.QuickKeyword;
import com.example.SeaTea.domain.diagnosis.enums.TastingNoteTypeCode;
import com.example.SeaTea.domain.diagnosis.exception.DiagnosisException;
import com.example.SeaTea.domain.diagnosis.exception.DiagnosisErrorStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DiagnosisQuickConverter {

    /**
     * Quick 진단 키워드(KW01~KW03)를 DiagnosisResponse 엔티티로 변환
     * - 저장용
     */
    public static List<DiagnosisResponse> fromKeywords(
            DiagnosisSession session,
            List<QuickKeyword> keywords
    ) {
        // 세션이 null인 경우 → 서비스 로직 오류 (잘못된 호출)
        if (session == null) {
            throw new DiagnosisException(DiagnosisErrorStatus._INVALID_STEP);
        }

        // 키워드가 없거나 비어있는 경우 → 클라이언트 요청 오류
        if (keywords == null || keywords.isEmpty()) {
            throw new DiagnosisException(DiagnosisErrorStatus._INVALID_KEYWORDS);
        }

        List<DiagnosisResponse> responses = new ArrayList<>();

        for (int i = 0; i < keywords.size(); i++) {
            QuickKeyword keyword = keywords.get(i);
            // 키워드 리스트 내부에 null이 포함된 경우 → 잘못된 요청
            if (keyword == null) {
                throw new DiagnosisException(DiagnosisErrorStatus._INVALID_KEYWORDS);
            }

            responses.add(
                    DiagnosisResponse.builder()
                            .session(session)
                            .itemCode(String.format("KW%02d", i + 1))
                            .answerCode(keyword.name())
                            .build()
            );
        }

        return responses;
    }

    /**
     * Quick 진단 API 응답 DTO 생성
     * - 응답용
     */
    public static DiagnosisQuickResponseDTO toResponseDTO(
            TastingNoteTypeCode resultType,
            List<QuickKeyword> keywords,
            Map<TastingNoteTypeCode, Integer> scores
    ) {
        return DiagnosisQuickResponseDTO.builder()
                .resultTypeCode(resultType)
                .keywords(keywords)
                .scores(scores)
                .build();
    }
}