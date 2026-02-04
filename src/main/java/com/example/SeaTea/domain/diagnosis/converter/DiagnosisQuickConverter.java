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
        if (session == null) {
            throw new DiagnosisException(
                DiagnosisErrorStatus._INVALID_STEP
            );//세션이 비어있는데 컨버터 호출
        }

        if (keywords == null || keywords.isEmpty()) {
            throw new DiagnosisException(
                DiagnosisErrorStatus._INVALID_STEP
            );//키워드를 아예 안보냄
        }

        List<DiagnosisResponse> responses = new ArrayList<>();

        for (int i = 0; i < keywords.size(); i++) {
            QuickKeyword keyword = keywords.get(i);
            if (keyword == null) {
                throw new DiagnosisException(
                    DiagnosisErrorStatus._INVALID_STEP
                );//키워드 중간에 null이 껴 있음.
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