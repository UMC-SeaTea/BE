package com.example.SeaTea.domain.diagnosis.dto.response;

import com.example.SeaTea.domain.diagnosis.enums.QuickKeyword;
import com.example.SeaTea.domain.diagnosis.enums.TastingNoteTypeCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

/**
 * 간단 진단(Quick) 응답 DTO
 */
@Getter
@Builder
@AllArgsConstructor
public class DiagnosisQuickResponseDTO {

    /** 최종 결과 타입 코드 */
    private TastingNoteTypeCode resultType;
    private List<QuickKeyword> keywords;
    private Map<TastingNoteTypeCode, Integer> scores;
}
