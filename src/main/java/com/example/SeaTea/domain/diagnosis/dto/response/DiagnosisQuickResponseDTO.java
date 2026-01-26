package com.example.SeaTea.domain.diagnosis.dto.response;

import com.example.SeaTea.domain.diagnosis.enums.TastingNoteTypeCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 간단 진단(Quick) 응답 DTO
 */
@Getter
@AllArgsConstructor
public class DiagnosisQuickResponseDTO {

    /** 최종 결과 타입 코드 */
    private TastingNoteTypeCode resultType;
}
