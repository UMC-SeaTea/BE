package com.example.SeaTea.domain.diagnosis.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DiagnosisSubmitResponseDTO {
    private String status;   // "DONE" or "NEED_MORE"
    private Integer nextStep; // status가 NEED_MORE일 때만 2
    private String resultTypeCode; // DONE일 때만 예: "NUTTY"

    private Long sessionId;
}
